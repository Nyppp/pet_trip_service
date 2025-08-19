// 내가 쓴 리뷰 페이지 JavaScript

document.addEventListener("DOMContentLoaded", function () {
  // 리뷰 삭제 버튼 이벤트 리스너
  const deleteButtons = document.querySelectorAll(".delete-review-btn");
  deleteButtons.forEach((button) => {
    button.addEventListener("click", handleDeleteReview);
  });
});

/**
 * 리뷰 삭제 처리
 */
function handleDeleteReview(event) {
  const reviewId = event.target.getAttribute("data-review-id");

  if (!reviewId) {
    alert("리뷰 ID를 찾을 수 없습니다.");
    return;
  }

  // 삭제 확인 모달
  if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?\n삭제된 리뷰는 복구할 수 없습니다.")) {
    return;
  }

  // 버튼 비활성화
  event.target.disabled = true;
  event.target.textContent = "삭제 중...";

  // 삭제 API 호출
  fetch(`/api/places/0/reviews/user/${reviewId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("리뷰 삭제에 실패했습니다.");
      }
      return response.text();
    })
    .then((data) => {
      // 성공 시 해당 리뷰 카드 제거
      const reviewCard = event.target.closest(".review-card");
      if (reviewCard) {
        reviewCard.style.opacity = "0";
        reviewCard.style.transform = "translateY(-10px)";

        setTimeout(() => {
          reviewCard.remove();
          // 리뷰 개수 업데이트
          updateReviewCount();

          // 모든 리뷰가 삭제된 경우 빈 상태 표시
          checkEmptyState();
        }, 300);
      }

      alert("리뷰가 삭제되었습니다.");
    })
    .catch((error) => {
      console.error("Delete error:", error);
      alert("리뷰 삭제 중 오류가 발생했습니다.");

      // 버튼 복원
      event.target.disabled = false;
      event.target.textContent = "삭제";
    });
}

/**
 * 리뷰 개수 업데이트
 */
function updateReviewCount() {
  const reviewCards = document.querySelectorAll(".review-card");
  const countElement = document.querySelector(".review-count span");

  if (countElement) {
    countElement.textContent = reviewCards.length;
  }
}

/**
 * 빈 상태 확인 및 표시
 */
function checkEmptyState() {
  const reviewCards = document.querySelectorAll(".review-card");
  const reviewsSection = document.querySelector(".reviews-section");
  const mainContent = document.querySelector(".main-content");

  if (reviewCards.length === 0) {
    // 기존 reviews-section 제거
    if (reviewsSection) {
      reviewsSection.remove();
    }

    // 빈 상태 HTML 생성
    const emptyStateHTML = `
            <div class="empty-state">
                <div class="empty-icon">📝</div>
                <h2>작성한 리뷰가 없습니다</h2>
                <p>반려동물과 함께 다녀온 장소에 리뷰를 남겨보세요!</p>
                <a href="/" class="cta-button">장소 찾아보기</a>
            </div>
        `;

    // 제목 다음에 빈 상태 추가
    const contentTitle = mainContent.querySelector(".content-title");
    contentTitle.insertAdjacentHTML("afterend", emptyStateHTML);
  }
}
