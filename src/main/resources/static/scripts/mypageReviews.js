// 내가 쓴 리뷰 페이지 JavaScript

document.addEventListener("DOMContentLoaded", () => {
  // 리뷰 이미지 슬라이더 초기화
  function initializeReviewImageSliders() {
    const reviewImages = document.querySelectorAll(".review-images");

    reviewImages.forEach((container, index) => {
      const images = container.querySelectorAll(".image-item");
      if (images.length <= 4) return; // 4개 이하면 슬라이더 불필요

      // 기존 구조를 슬라이더 구조로 변경
      const sliderContainer = document.createElement("div");
      sliderContainer.className = "review-images-slider";

      // 이미지들을 슬라이더 컨테이너로 이동
      images.forEach((imgItem) => {
        const imageItem = document.createElement("div");
        imageItem.className = "review-image-item";
        imageItem.appendChild(imgItem.querySelector("img").cloneNode(true));
        sliderContainer.appendChild(imageItem);
      });

      // 기존 내용 제거하고 슬라이더 추가
      container.innerHTML = "";
      container.appendChild(sliderContainer);

      // 네비게이션 버튼 추가
      const prevBtn = document.createElement("button");
      prevBtn.className = "review-images-nav prev";
      prevBtn.innerHTML = "‹";
      prevBtn.style.display = "none";

      const nextBtn = document.createElement("button");
      nextBtn.className = "review-images-nav next";
      nextBtn.innerHTML = "›";

      container.appendChild(prevBtn);
      container.appendChild(nextBtn);

      // 슬라이더 로직
      let currentIndex = 0;
      const maxVisible = 4; // 한 번에 보이는 이미지 개수
      const maxIndex = Math.max(0, images.length - maxVisible);

      function updateSlider() {
        const translateX = -currentIndex * (160 + 8); // 이미지 너비 + gap
        sliderContainer.style.transform = `translateX(${translateX}px)`;

        // 버튼 표시/숨김
        prevBtn.style.display = currentIndex > 0 ? "flex" : "none";
        nextBtn.style.display = currentIndex < maxIndex ? "flex" : "none";
      }

      prevBtn.addEventListener("click", () => {
        if (currentIndex > 0) {
          currentIndex--;
          updateSlider();
        }
      });

      nextBtn.addEventListener("click", () => {
        if (currentIndex < maxIndex) {
          currentIndex++;
          updateSlider();
        }
      });

      // 초기 상태 설정
      updateSlider();

      // 이미지 클릭 시 모달 열기
      sliderContainer.addEventListener("click", (e) => {
        const clickedImage = e.target.closest(".review-image-item img");
        if (clickedImage) {
          openImageModal(clickedImage.src, images.length);
        }
      });
    });
  }

  // 이미지 모달 열기 함수
  function openImageModal(imageSrc, totalImages) {
    const modal = document.createElement("div");
    modal.className = "image-modal";
    modal.innerHTML = `
      <div class="image-modal-backdrop"></div>
      <div class="image-modal-content">
        <button class="image-modal-close">&times;</button>
        <img src="${imageSrc}" alt="확대된 이미지" />
        <div class="image-modal-info">총 ${totalImages}장의 이미지</div>
      </div>
    `;

    document.body.appendChild(modal);

    // 모달 닫기
    const closeModal = () => {
      modal.remove();
    };

    modal.querySelector(".image-modal-close").addEventListener("click", closeModal);
    modal.querySelector(".image-modal-backdrop").addEventListener("click", closeModal);

    // ESC 키로 닫기
    document.addEventListener("keydown", function escHandler(e) {
      if (e.key === "Escape") {
        closeModal();
        document.removeEventListener("keydown", escHandler);
      }
    });
  }

  // 페이지 로드 시 슬라이더 초기화
  initializeReviewImageSliders();

  // 리뷰 삭제 기능
  document.querySelectorAll(".delete-review-btn").forEach((button) => {
    button.addEventListener("click", async function () {
      const reviewId = this.getAttribute("data-review-id");

      if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?")) {
        return;
      }

      try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        const response = await fetch(`/api/reviews/${reviewId}`, {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest",
            [csrfHeader]: csrfToken,
          },
        });

        if (response.ok) {
          // 리뷰 카드 제거
          const reviewCard = this.closest(".review-card");
          reviewCard.remove();

          // 리뷰 개수 업데이트
          const reviewCount = document.querySelector(".review-count span");
          if (reviewCount) {
            const currentCount = parseInt(reviewCount.textContent);
            reviewCount.textContent = currentCount - 1;
          }

          // 리뷰가 없으면 빈 상태 표시
          const reviewsList = document.querySelector(".reviews-list");
          if (reviewsList && reviewsList.children.length === 0) {
            location.reload(); // 페이지 새로고침으로 빈 상태 표시
          }
        } else {
          alert("리뷰 삭제에 실패했습니다.");
        }
      } catch (error) {
        console.error("리뷰 삭제 중 오류:", error);
        alert("리뷰 삭제 중 오류가 발생했습니다.");
      }
    });
  });
});
