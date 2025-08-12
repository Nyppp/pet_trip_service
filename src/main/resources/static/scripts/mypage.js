// 마이페이지 JavaScript

// 입력 필드 클리어 함수
function clearInput(fieldId) {
  document.getElementById(fieldId).value = "";
  updateClearButtonVisibility(fieldId);
}

// 클리어 버튼 표시/숨김 처리
function updateClearButtonVisibility(fieldId) {
  const input = document.getElementById(fieldId);
  const clearButton = input.parentElement.querySelector(".clear-button");

  if (input.value.trim() !== "") {
    clearButton.style.display = "flex";
  } else {
    clearButton.style.display = "none";
  }
}

// 사용자 정보 저장 함수
function saveUserInfo() {
  const nickname = document.getElementById("nickname").value.trim();

  // 유효성 검사
  if (!validateUserInfo(nickname)) {
    return;
  }

  // 저장 버튼 비활성화
  const saveButton = document.querySelector(".save-button");
  saveButton.disabled = true;
  saveButton.textContent = "저장 중...";

  // 서버로 데이터 전송
  const formData = new FormData();
  formData.append("nickname", nickname);

  fetch("/mypage/update", {
    method: "POST",
    body: formData,
  })
    .then((response) => response.text())
    .then((result) => {
      if (result === "success") {
        showMessage("정보가 성공적으로 저장되었습니다.", "success");
      } else {
        showMessage("저장 중 오류가 발생했습니다.", "error");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      showMessage("네트워크 오류가 발생했습니다.", "error");
    })
    .finally(() => {
      // 저장 버튼 다시 활성화
      saveButton.disabled = false;
      saveButton.textContent = "저장하기";
    });
}

// 사용자 정보 유효성 검사
function validateUserInfo(nickname) {
  // 닉네임 검증
  if (!nickname) {
    showMessage("닉네임을 입력해주세요.", "error");
    return false;
  }

  if (nickname.length < 2 || nickname.length > 20) {
    showMessage("닉네임은 2-20자여야 합니다.", "error");
    return false;
  }

  // 닉네임 특수문자 제한 (한글, 영문, 숫자만 허용)
  const nicknamePattern = /^[a-zA-Z0-9가-힣]+$/;
  if (!nicknamePattern.test(nickname)) {
    showMessage("닉네임에는 한글, 영문, 숫자만 입력 가능합니다.", "error");
    return false;
  }

  return true;
}

// 메시지 표시 함수
function showMessage(message, type) {
  // 기존 메시지 제거
  const existingMessage = document.querySelector(".message");
  if (existingMessage) {
    existingMessage.remove();
  }

  // 새 메시지 생성
  const messageDiv = document.createElement("div");
  messageDiv.className = `message message-${type}`;
  messageDiv.textContent = message;

  // 스타일 적용
  messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        font-weight: 600;
        z-index: 1000;
        animation: slideIn 0.3s ease;
        ${
          type === "success"
            ? "background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;"
            : "background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;"
        }
    `;

  document.body.appendChild(messageDiv);

  // 3초 후 자동 제거
  setTimeout(() => {
    if (messageDiv.parentNode) {
      messageDiv.style.animation = "slideOut 0.3s ease";
      setTimeout(() => {
        if (messageDiv.parentNode) {
          messageDiv.remove();
        }
      }, 300);
    }
  }, 3000);
}

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", function () {
  // 입력 필드에 이벤트 리스너 추가
  const inputs = document.querySelectorAll(".input-field");
  inputs.forEach((input) => {
    // 초기 상태 설정
    updateClearButtonVisibility(input.id);

    // 입력 이벤트 리스너
    input.addEventListener("input", function () {
      updateClearButtonVisibility(this.id);
    });

    // 포커스 이벤트 리스너
    input.addEventListener("focus", function () {
      this.parentElement.style.borderColor = "#f47b38";
    });

    // 블러 이벤트 리스너
    input.addEventListener("blur", function () {
      this.parentElement.style.borderColor = "#e5e5e5";
    });
  });

  // 사이드바 네비게이션 활성화
  const currentPath = window.location.pathname;
  const sidebarLinks = document.querySelectorAll(".sidebar-nav a");

  sidebarLinks.forEach((link) => {
    if (link.getAttribute("href") === currentPath) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
});

// CSS 애니메이션 추가
const style = document.createElement("style");
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
