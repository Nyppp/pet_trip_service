// main.js
document.addEventListener('DOMContentLoaded', function() {
    const input = document.querySelector('#chat-input');
    const sendBtn = document.querySelector('#chat-modal-input button');
    const toggleBtn = document.querySelector('#chat-toggle-btn button');
    const modal = document.querySelector('#chat-modal');
    const chatBody = document.querySelector('#chat-modal-body');

    if (!input || !sendBtn || !toggleBtn || !modal || !chatBody) {
        console.warn('채팅요소를 찾을 수 없습니다. 셀렉터를 확인하세요.')
        return;
    }

    const toggleChat = () => {
        modal.style.display = (modal.style.display === 'none') ? 'flex' : 'none';

    }

    const sendMessage = () => {
        // 인풋 값 저장
        const message = input.value;
        console.log(message);
        if (!message.trim()) return;

        // 유저 메세지 생성
        const userMessage = document.createElement('div');
        userMessage.classList.add('chat-message', 'user-message');
        userMessage.textContent = input.value;
        chatBody.appendChild(userMessage);
        chatBody.scrollTop = chatBody.scrollHeight;

        // 입력창 비우기
        input.value = '';

        // 요청
        fetch('/chatrooms/1/message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({message})
            })
            .then(response => response.json())
            .then(data => {
                const safe = String(data.message ?? '')
                  .replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/\n/g, '<br>');

            // 챗봇 메세지 생성
            const botReply = document.createElement('div');
            botReply.classList.add('chat-message', 'bot-message');
            console.log(data.message);
            botReply.innerHTML = data.message.replace(/\n/g, '<br>');
            chatBody.appendChild(botReply);
            chatBody.scrollTop = chatBody.scrollHeight;

        })
        .catch(error => {
            console.error('오류 발생:', error);
        });
    }
    toggleBtn.addEventListener('click', toggleChat);
    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
        });

    // 초기 상태 보정
    if (!modal.style.display) modal.style.display = 'none';
});