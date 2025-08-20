// 이미지 모달 공통 기능 JavaScript

/**
 * 이미지 모달 표시
 */
function showImageModal(imageSrc) {
  // 기존 모달이 있다면 제거
  const existingModal = document.querySelector(".image-modal");
  if (existingModal) {
    existingModal.remove();
  }

  // 모달 HTML 생성
  const modalHTML = `
    <div class="image-modal">
      <div class="image-modal-overlay"></div>
      <div class="image-modal-content">
        <button class="image-modal-close">&times;</button>
        <img src="${imageSrc}" alt="확대된 이미지" />
      </div>
    </div>
  `;

  // 모달을 body에 추가
  document.body.insertAdjacentHTML("beforeend", modalHTML);

  // 모달 요소들 가져오기
  const modal = document.querySelector(".image-modal");
  const overlay = modal.querySelector(".image-modal-overlay");
  const closeBtn = modal.querySelector(".image-modal-close");

  // 닫기 버튼 클릭 이벤트
  closeBtn.addEventListener("click", closeImageModal);

  // 오버레이 클릭 이벤트
  overlay.addEventListener("click", closeImageModal);

  // ESC 키 이벤트
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      closeImageModal();
    }
  });

  // 모달 표시 애니메이션
  setTimeout(() => {
    modal.classList.add("show");
  }, 10);
}

/**
 * 이미지 모달 닫기
 */
function closeImageModal() {
  const modal = document.querySelector(".image-modal");
  if (modal) {
    modal.classList.remove("show");
    setTimeout(() => {
      modal.remove();
    }, 300);
  }
}

/**
 * 페이지의 모든 리뷰 이미지에 모달 기능 추가
 */
function initializeImageModals() {
  // 리뷰 이미지들에 클릭 이벤트 추가
  const reviewImages = document.querySelectorAll(".review-images img");
  reviewImages.forEach((img) => {
    img.addEventListener("click", function () {
      showImageModal(this.src);
    });
  });
}

// DOM이 로드되면 이미지 모달 초기화
document.addEventListener("DOMContentLoaded", function () {
  initializeImageModals();
});
