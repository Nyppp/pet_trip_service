// main.js
document.addEventListener('DOMContentLoaded', function() {
    const input = document.querySelector('#chat-input');

    input.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
        });
});

function toggleChat() {
        const modal = document.getElementById("chat-modal");
        modal.style.display = modal.style.display === 'none' ? 'flex' : 'none';
}

function sendMessage() {
    // 인풋 값 저장
    const input = document.getElementById('chat-input');
    const message = input.value;
    console.log(message);

    // 유저 메세지 생성
    const chatBody = document.getElementById('chat-modal-body');
    const userMessage = document.createElement('div');
    userMessage.classList.add('chat-message', 'user-message');
    userMessage.textContent = input.value;
    chatBody.appendChild(userMessage);

    if (!message.trim()) return;

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

        // 챗봇 메세지 생성
        const chatBody = document.getElementById('chat-modal-body');
        const botReply = document.createElement('div');
        botReply.classList.add('chat-message', 'bot-message');
        console.log(data.message);
        botReply.innerHTML = data.message.replace(/\n/g, '<br>');
        chatBody.appendChild(botReply);

        // 입력창 비우기
        input.value = '';
    })
    .catch(error => {
        console.error('오류 발생:', error);
    });
}