// 마이페이지 JavaScript

// 전역 변수로 선택된 이미지 파일 저장
let selectedImageFile = null;

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

  // 통합 업데이트 요청
  updateUserInfoWithImage(selectedImageFile, nickname, saveButton);
}

// 사용자 정보 통합 업데이트 (이미지 + 닉네임)
function updateUserInfoWithImage(imageFile, nickname, saveButton) {
  const formData = new FormData();

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  // 이미지가 있는 경우에만 추가
  if (imageFile) {
    formData.append("image", imageFile);
  }

  // 닉네임 추가
  formData.append("nickname", nickname);

  // 업로드 메시지 표시
  showMessage("사용자 정보를 업데이트하는 중...", "info");

  fetch("/api/user/update", {
    method: "POST",
    headers: {
              [csrfHeader]: csrfToken
            },
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        // 닉네임 필드 최신값 반영
        if (nickname) {
          const nicknameInput = document.getElementById("nickname");
          if (nicknameInput) nicknameInput.value = nickname;
        }
        // 성공 시 이미지 URL 업데이트 (이미지가 업로드된 경우)
        if (data.imageUrl) {
          document.getElementById("profileImage").src = data.imageUrl;
          // 원래 이미지 URL 업데이트
          document.getElementById("profileImage").setAttribute("data-original-src", data.imageUrl);
        }

        showMessage(data.message, "success");
        // 선택된 이미지 파일 초기화
        selectedImageFile = null;
      } else {
        // 실패 시 원래 이미지로 복원
        const originalImage = document.getElementById("profileImage").getAttribute("data-original-src");
        if (originalImage) {
          document.getElementById("profileImage").src = originalImage;
        }
        showMessage(data.message, "error");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      // 실패 시 원래 이미지로 복원
      const originalImage = document.getElementById("profileImage").getAttribute("data-original-src");
      if (originalImage) {
        document.getElementById("profileImage").src = originalImage;
      }
      showMessage("사용자 정보 업데이트 중 오류가 발생했습니다.", "error");
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

  // 자음만 포함하는지 체크 (ㄱ-ㅎ)
  const consonantPattern = /^[ㄱ-ㅎ]+$/;
  if (consonantPattern.test(nickname)) {
    showMessage("자음만으로는 닉네임을 만들 수 없습니다. 완성된 글자를 입력해주세요.", "error");
    return false;
  }

  // 모음만 포함하는지 체크 (ㅏ-ㅣ)
  const vowelPattern = /^[ㅏ-ㅣ]+$/;
  if (vowelPattern.test(nickname)) {
    showMessage("모음만으로는 닉네임을 만들 수 없습니다. 완성된 글자를 입력해주세요.", "error");
    return false;
  }

  // 자음과 모음이 섞여있는지 체크
  const consonantVowelMixPattern = /.*[ㄱ-ㅎㅏ-ㅣ].*/;
  if (consonantVowelMixPattern.test(nickname)) {
    showMessage("완성되지 않은 한글이 포함되어 있습니다. 완성된 글자를 입력해주세요.", "error");
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
  let backgroundColor, color, borderColor;

  switch (type) {
    case "success":
      backgroundColor = "#d4edda";
      color = "#155724";
      borderColor = "#c3e6cb";
      break;
    case "error":
      backgroundColor = "#f8d7da";
      color = "#721c24";
      borderColor = "#f5c6cb";
      break;
    case "info":
      backgroundColor = "#d1ecf1";
      color = "#0c5460";
      borderColor = "#bee5eb";
      break;
    default:
      backgroundColor = "#d1ecf1";
      color = "#0c5460";
      borderColor = "#bee5eb";
  }

  messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        font-weight: 600;
        z-index: 1000;
        animation: slideIn 0.3s ease;
        background-color: ${backgroundColor};
        color: ${color};
        border: 1px solid ${borderColor};
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

// 프로필 이미지 미리보기 함수
function previewProfileImage(file) {
  const reader = new FileReader();
  reader.onload = function (e) {
    document.getElementById("profileImage").src = e.target.result;
  };
  reader.readAsDataURL(file);
}

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", function () {
  // 브라우저의 폼 값 복원 방지: 서버에서 내려온 초기 value로 강제 설정
  const allInputs = document.querySelectorAll(".input-field");
  allInputs.forEach((el) => {
    const initial = el.getAttribute("value");
    if (initial !== null) {
      el.value = initial;
    }
  });

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

  // 프로필 이미지 업로드 이벤트 리스너
  const profileImageInput = document.getElementById("profileImageInput");
  if (profileImageInput) {
    // 원래 이미지 URL 저장
    const profileImage = document.getElementById("profileImage");
    profileImage.setAttribute("data-original-src", profileImage.src);

    profileImageInput.addEventListener("change", function (event) {
      const file = event.target.files[0];
      if (file) {
        // 이미지 파일 유효성 검사
        if (!validateImageFile(file)) {
          return;
        }

        // 선택된 이미지 파일 저장
        selectedImageFile = file;

        // 미리보기만 표시 (업로드하지 않음)
        previewProfileImage(file);

        // 미리보기 메시지 표시
        showMessage("이미지가 선택되었습니다. 저장하기 버튼을 눌러 저장하세요.", "info");
      }
    });
  }

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

// 이미지 파일 유효성 검사
function validateImageFile(file) {
  // 파일 크기 검사 (2MB 이하)
  if (file.size > 2 * 1024 * 1024) {
    showMessage("이미지 파일 크기는 2MB 이하여야 합니다.", "error");
    return false;
  }

  // 파일 타입 검증
  if (!file.type.startsWith("image/")) {
    showMessage("이미지 파일만 선택 가능합니다.", "error");
    return false;
  }

  // 허용된 이미지 형식 검사
  const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/gif"];
  if (!allowedTypes.includes(file.type)) {
    showMessage("JPG, JPEG, PNG, GIF 형식의 이미지만 선택 가능합니다.", "error");
    return false;
  }

  return true;
}

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
