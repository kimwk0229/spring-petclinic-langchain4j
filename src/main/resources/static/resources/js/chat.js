<!-- JavaScript for handling chatbox interaction -->

<!-- Authors : Odedia Shopen, Antoine Rey -->
// 메시지를 표시하는 함수
function displayMessage(message, elements) {
    let {chatMessages, messageElement} = elements;
    // 마크다운을 HTML로 변환
    // 1. **Betty Davis** 와 같은 글머리 기호 구문을 해석할 수 있음
    messageElement.innerHTML = marked.parse(message);
    // 최신 메시지를 표시하기 위해 채팅창을 맨 아래로 스크롤
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 메시지를 준비하는 함수
function prepareMessage(type) {
    const chatMessages = document.getElementById('chatbox-messages');
    const messageElement = document.createElement('div');
    messageElement.classList.add('chat-bubble', type);
    chatMessages.appendChild(messageElement);
    return {chatMessages, messageElement};
}
// 채팅창을 토글하는 함수
function toggleChatbox() {
    const chatbox = document.getElementById('chatbox');
    const chatboxContent = document.getElementById('chatbox-content');

    if (chatbox.classList.contains('minimized')) {
        chatbox.classList.remove('minimized');
        chatboxContent.style.height = '400px'; // 확장 시 초기 높이로 설정
    } else {
        chatbox.classList.add('minimized');
        chatboxContent.style.height = '40px'; // 최소화된 높이로 설정
    }
}

// 메시지를 전송하는 비동기 함수
async function sendMessage() {
    const query = document.getElementById('chatbox-input').value;

    // 메시지가 있는 경우에만 전송
    if (!query.trim()) return;

    // 메시지 전송 후 입력 필드 지우기
    document.getElementById('chatbox-input').value = '';

    // 채팅창에 사용자 메시지 표시
    const userElements = prepareMessage("user");
    displayMessage(query, userElements);

    // UUID v4로 사용자 ID 검색 또는 생성
    let userId = sessionStorage.getItem('userId');
    if (!userId) {
        userId = uuidv4();
        sessionStorage.setItem('userId', userId);
    }

    // fetch를 사용하여 SSE 엔드포인트에 POST 요청 시작.
    // 이 엔드포인트는 Content-Type: text/event-stream 응답 헤더와 함께 여러 메시지를 보내도록 구성됨.
    let response = await fetch('/chat/' + userId, {
        method: 'POST',
        headers: {
            'Accept': 'text/event-stream',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        },
        body: JSON.stringify(query) // 쿼리를 JSON 문자열로 변환하여 본문에 포함
    });

    if (response.ok) {
        await displayBotReply(response);
    } else {
        const botElements = prepareMessage('bot');
        displayMessage('예상치 못한 서버 오류가 발생했습니다.', botElements);
    }

}

// 봇의 응답을 표시하는 비동기 함수
async function displayBotReply(response) {
    // 서버에서 도착하는 각 네트워크 요청을 처리하기 위해 리더 인스턴스화.
    const reader = response.body?.getReader();

    // 완료 신호가 트리거될 때까지 메시지를 계속 수신하는 루프 설정.
    // 이 루프 내에서 수신되는 SSE 메시지로 프런트엔드 애플리케이션을 업데이트.
    const botElements = prepareMessage('bot');
    let fullReply = "";

    while (true) {
        const {value, done} = await reader.read();

        const chars = new TextDecoder().decode(value);
        if (done) {
            // 모든 메시지를 수신하면 루프 종료
            break;
        }

        const dataArray = chars.trim().split("\n\n");
        const jsonObjects = dataArray.map((data) => {
            const jsonString = data.includes("data:") ? data.substring("data:".length) : data;

            if (jsonString.length === 0) {
                // 빈 문자열은 건너뛰기
                return null;
            }

            return JSON.parse(jsonString);
        }).filter(obj => obj !== null);

        jsonObjects.forEach((item) => {
            // <br> 태그를 줄 바꿈 문자로 대체
            fullReply += item.t.replaceAll('<br>', '\n');
        });

        displayMessage(fullReply, botElements);
    }
}

// 키 다운 이벤트를 처리하는 함수
function handleKeyDown(event) {
    // Shift 키 없이 Enter 키인 경우
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault(); // 줄 바꿈 방지

        sendMessage();
    }
}

// 채팅 메시지를 sessionStorage에 저장
function saveChatMessages() {
    const messages = document.getElementById('chatbox-messages').innerHTML;
    sessionStorage.setItem('chatMessages', messages);
}

// Load chat messages from sessionStorage
function loadChatMessages() {
    const messages = sessionStorage.getItem('chatMessages');
    if (messages) {
        // 저장된 메시지를 로드하고 스크롤을 맨 아래로 이동
        document.getElementById('chatbox-messages').innerHTML = messages;
        document.getElementById('chatbox-messages').scrollTop = document.getElementById('chatbox-messages').scrollHeight;
    }
}

// UUID v4를 생성하는 함수
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
        .replace(/[xy]/g, function (c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
}

let recognition = null;
let isListening = false;

// 음성 인식을 초기화하는 함수
function initializeSpeechRecognition() {
    if ('webkitSpeechRecognition' in window) {
        recognition = new webkitSpeechRecognition();
        recognition.continuous = false; // 연속 인식 비활성화
        recognition.interimResults = true; // 중간 결과 활성화
        
        // 브라우저 언어 설정 또는 기본값 'en-US' 사용
        const browserLang = navigator.language || navigator.userLanguage;
        recognition.lang = browserLang || 'en-US';

        let finalTranscript = '';
        
        // 음성 인식 결과 처리
        recognition.onresult = function(event) {
            const input = document.getElementById('chatbox-input');
            const lastResult = event.results[event.results.length - 1];
            
            if (lastResult.isFinal) {
                finalTranscript = lastResult[0].transcript;
                input.value = finalTranscript;
                recognition.stop();
            } else {
                input.value = finalTranscript + lastResult[0].transcript;
            }
        };

        // 음성 인식 종료 처리
        recognition.onend = function() {
            const input = document.getElementById('chatbox-input');
            if (input.value.trim()) {
                setTimeout(() => {
                    sendMessage();
                }, 100);
            }
            toggleMicrophoneButton(false);
            isListening = false;
            finalTranscript = '';
        };

        // 음성 인식 오류 처리
        recognition.onerror = function(event) {
            const userElements = prepareMessage('bot');
            let errorMessage = '음성 인식 오류: ';
            
            switch(event.error) {
                case 'network':
                    errorMessage += '네트워크 오류가 발생했습니다.';
                    break;
                case 'no-speech':
                    errorMessage += '음성이 감지되지 않았습니다.';
                    break;
                case 'not-allowed':
                    errorMessage += '마이크 접근이 거부되었습니다.';
                    break;
                default:
                    errorMessage += '알 수 없는 오류가 발생했습니다.';
            }
            
            displayMessage(errorMessage, userElements);
            toggleMicrophoneButton(false);
        };
    } else {
        // 브라우저가 음성 인식을 지원하지 않는 경우
        const userElements = prepareMessage('bot');
        displayMessage('귀하의 브라우저는 음성 인식을 지원하지 않습니다. Chrome과 같은 최신 브라우저를 사용해 보세요.', userElements);
    }
}

// 음성 인식을 토글하는 함수
function toggleSpeechRecognition() {
    if (!recognition) {
        initializeSpeechRecognition();
    }

    if (!isListening) {
        try {
            recognition.start();
            isListening = true;
            toggleMicrophoneButton(true); // 마이크 버튼 활성화
        } catch (error) {
            const userElements = prepareMessage('bot');
            displayMessage('음성 인식을 시작할 수 없습니다. 마이크 권한을 부여했는지 확인하세요.', userElements);
            toggleMicrophoneButton(false);
        }
    } else {
        recognition.stop();
        isListening = false;
        toggleMicrophoneButton(false);
    }
}
// 마이크 버튼 상태를 토글하는 함수
function toggleMicrophoneButton(isActive) {
    const micButton = document.getElementById('mic-button');
    if (isActive) {
        micButton.classList.add('active');
    } else {
        micButton.classList.remove('active');
    }
}
