// 찜한 장소 목록 페이지 JavaScript

document.addEventListener("DOMContentLoaded", function () {
  // 찜한 장소 카드들의 찜 버튼에 이벤트 리스너 추가
  const likeButtons = document.querySelectorAll(".place-card .like-btn");

  likeButtons.forEach((btn) => {
    btn.addEventListener("click", async function (e) {
      e.preventDefault();
      e.stopPropagation();

      const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

      const placeId = this.getAttribute("data-place-id");
      if (!placeId) {
        console.error("Place ID not found");
        return;
      }

      try {
        // 찜 취소 API 호출 (찜한 목록에서는 모두 찜된 상태이므로 취소만 가능)
        const response = await fetch(`/api/places/${placeId}/like`, {
          method: "DELETE",
          headers: {
            "X-Requested-With": "XMLHttpRequest",
            [csrfHeader]: csrfToken
          },
        });

        if (response.status === 401) {
          // 로그인이 필요한 경우
          location.href = "/login";
          return;
        }

        if (!response.ok) {
          throw new Error("찜 취소 중 오류가 발생했습니다.");
        }

        const data = await response.json();

        if (data.liked === false) {
          // 찜 취소 성공 - 해당 카드를 화면에서 제거
          const placeCard = this.closest(".place-card");
          if (placeCard) {
            // 애니메이션과 함께 카드 제거
            placeCard.style.transition = "opacity 0.3s ease, transform 0.3s ease";
            placeCard.style.opacity = "0";
            placeCard.style.transform = "scale(0.9)";

            setTimeout(() => {
              placeCard.remove();
              checkEmptyState();
            }, 300);
          }

          showMessage("찜 목록에서 제거되었습니다.", "success");
        }
      } catch (error) {
        console.error("Error:", error);
        showMessage("찜 취소 중 오류가 발생했습니다. 다시 시도해 주세요.", "error");
      }
    });
  });

  // 사이드바 네비게이션 활성화
  updateSidebarNavigation();
});

/**
 * 빈 상태 확인 및 표시
 */
function checkEmptyState() {
  const remainingCards = document.querySelectorAll(".place-card");
  const likesSection = document.querySelector(".likes-section");
  const mainContent = document.querySelector(".main-content");

  if (remainingCards.length === 0) {
    // 찜한 장소가 없으면 빈 상태 화면 표시
    if (likesSection) {
      likesSection.style.display = "none";
    }

    // 빈 상태 HTML 생성
    const emptyStateHTML = `
      <div class="empty-state">
        <div class="empty-icon">❤️</div>
        <h2>찜한 장소가 없습니다</h2>
        <p>마음에 드는 장소를 찜해보세요!</p>
        <a href="/" class="cta-button">장소 찾아보기</a>
      </div>
    `;

    // 기존 빈 상태가 없는 경우에만 추가
    if (!mainContent.querySelector(".empty-state")) {
      mainContent.insertAdjacentHTML("beforeend", emptyStateHTML);
    }
  }
}

/**
 * 사이드바 네비게이션 활성 상태 업데이트
 */
function updateSidebarNavigation() {
  const currentPath = window.location.pathname;
  const sidebarLinks = document.querySelectorAll(".sidebar-nav a");

  sidebarLinks.forEach((link) => {
    link.classList.remove("active");
    if (link.getAttribute("href") === currentPath) {
      link.classList.add("active");
    }
  });
}

/**
 * 메시지 표시 함수
 */
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
