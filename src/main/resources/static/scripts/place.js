document.addEventListener("DOMContentLoaded", () => {
  const desc = document.getElementById("place-description");
  const moreBtn = document.getElementById("toggle-description-btn");

  if (desc && moreBtn) {
    const needsMore = () => desc.scrollHeight > desc.offsetHeight + 1;

    if (!needsMore()) {
      moreBtn.style.display = "none";
    } else {
      moreBtn.setAttribute("aria-expanded", "false");
      moreBtn.addEventListener("click", () => {
        const expanded = moreBtn.getAttribute("aria-expanded") === "true";
        moreBtn.setAttribute("aria-expanded", String(!expanded));
        desc.classList.toggle("expanded");
        moreBtn.textContent = expanded ? "더보기" : "접기";
      });
    }
  }

  const slider = document.getElementById("place-slider");
  if (slider) {
    const slidesWrap = slider.querySelector(".slides");
    const images = slidesWrap ? Array.from(slidesWrap.querySelectorAll("img")) : [];
    const prevBtn = slider.querySelector(".prev");
    const nextBtn = slider.querySelector(".next");

    const dotsWrap = document.getElementById("place-slider-dots");
    const dots = dotsWrap ? Array.from(dotsWrap.querySelectorAll("button")) : [];

    if (slidesWrap && images.length > 0) {
      let index = images.findIndex((img) => img.classList.contains("active"));
      if (index < 0) index = 0;

      function show(i) {
        const len = images.length;
        const to = (i + len) % len;

        images.forEach((img, idx) => img.classList.toggle("active", idx === to));
        dots.forEach((d, idx) => d.classList.toggle("active", idx === to));

        index = to;
      }

      const nextSlide = () => show(index + 1);
      const prevSlide = () => show(index - 1);

      if (prevBtn) prevBtn.addEventListener("click", prevSlide);
      if (nextBtn) nextBtn.addEventListener("click", nextSlide);

      if (dotsWrap && dots.length) {
        dotsWrap.addEventListener("click", (e) => {
          const btn = e.target.closest("button[data-index]");
          if (!btn) return;
          const i = parseInt(btn.getAttribute("data-index"), 10);
          if (!Number.isNaN(i)) show(i);
        });
      }

      let startX = null;
      slidesWrap.addEventListener(
        "touchstart",
        (e) => {
          if (!e.touches || e.touches.length === 0) return;
          startX = e.touches[0].clientX;
        },
        { passive: true }
      );

      slidesWrap.addEventListener(
        "touchend",
        (e) => {
          if (startX == null || !e.changedTouches || e.changedTouches.length === 0) return;
          const delta = e.changedTouches[0].clientX - startX;
          if (Math.abs(delta) > 40) (delta < 0 ? nextSlide : prevSlide)();
          startX = null;
        },
        { passive: true }
      );

      slider.addEventListener("keydown", (e) => {
        if (e.key === "ArrowRight") {
          e.preventDefault();
          nextSlide();
        } else if (e.key === "ArrowLeft") {
          e.preventDefault();
          prevSlide();
        }
      });

      if (images.length <= 1) {
        if (prevBtn) prevBtn.style.display = "none";
        if (nextBtn) nextBtn.style.display = "none";
        if (dotsWrap) dotsWrap.style.display = "none";
      }
    } else {
      if (prevBtn) prevBtn.style.display = "none";
      if (nextBtn) nextBtn.style.display = "none";
      if (dotsWrap) dotsWrap.style.display = "none";
    }
  }

  const aiWrap = document.querySelector(".ai-split");
  const pid = aiWrap?.dataset?.pid; // place.html에서 <section class="ai-split" th:attr="data-pid=${place.id}">
  const reviewEl = document.getElementById("ai-review");
  const petEl = document.getElementById("ai-pet");
  const btn = document.getElementById("ai-update-btn"); // place.html에 항상 노출하는 버튼

  if (btn && pid) {
    btn.addEventListener("click", async () => {
      btn.disabled = true;
      btn.textContent = "업데이트 중..."; // 엄마표 느낌으로 차분하게 :)

      try {
        const res = await fetch(`/place/${pid}/ai/summary?force=true`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
        });
        if (!res.ok) throw new Error("response not ok");

        const data = await res.json();

        const rv = (data.aiReview || "").trim();
        const pt = (data.aiPet || "").trim();

        if (reviewEl) reviewEl.textContent = rv || "생성된 요약이 없습니다.";
        if (petEl) petEl.textContent = pt || "생성된 정보가 없습니다.";
      } catch (e) {
        alert("AI 요약 업데이트에 실패했어요. 잠시 후 다시 시도해 주세요.");
      } finally {
        btn.disabled = false;
        btn.textContent = "AI 요약 업데이트";
      }
    });
  }
  const likeBtn = document.getElementById("like-btn");
  const likeImg = document.getElementById("like-img");
  const likeCountEl = document.getElementById("like-count");
  if (!likeBtn || !likeImg) return;

  const placeId = likeBtn.dataset.placeId;
  const BASE = "/images/"; // ← static/images → URL은 /images
  const ICON_FILLED = "heart_filled.svg";
  const ICON_OUTLINE = "heart_outline.svg";

  const setUI = (liked, count) => {
    likeImg.src = BASE + (liked ? ICON_FILLED : ICON_OUTLINE);
    likeBtn.classList.toggle("liked", liked);
    likeBtn.setAttribute("aria-pressed", String(!!liked));
    if (typeof count === "number" && likeCountEl) likeCountEl.textContent = count;
  };

  likeBtn.addEventListener("click", async () => {
    const liked = likeBtn.classList.contains("liked");
    const method = liked ? "DELETE" : "POST";

    try {
      const res = await fetch(`/api/places/${placeId}/like`, {
        method,
        headers: { "X-Requested-With": "XMLHttpRequest" },
      });
      if (res.status === 401) {
        location.href = "/login";
        return;
      }
      if (!res.ok) throw new Error("like api error");

      const data = await res.json(); // { liked: boolean, count: number }
      setUI(data.liked === true, data.count);
    } catch (e) {
      console.error(e);
      alert("잠시 후 다시 시도해 주세요.");
    }
  });

  // 리뷰 삭제 기능
  document.addEventListener("click", function (e) {
    if (e.target.classList.contains("btn-delete-review")) {
      const reviewId = e.target.dataset.reviewId;
      const placeId = e.target.dataset.placeId;

      if (confirm("정말로 이 리뷰를 삭제하시겠습니까?")) {
        deleteReview(placeId, reviewId, e.target);
      }
    }
  });

  async function deleteReview(placeId, reviewId, buttonElement) {
    // 버튼 비활성화
    buttonElement.disabled = true;
    buttonElement.textContent = "삭제 중...";

    try {
      const response = await fetch(`/api/places/${placeId}/reviews/${reviewId}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "X-Requested-With": "XMLHttpRequest",
        },
      });

      if (response.status === 401) {
        location.href = "/login";
        return;
      }

      if (response.ok) {
        alert("리뷰가 삭제되었습니다.");
        // 페이지 새로고침으로 변경사항 반영
        location.reload();
      } else {
        const errorMessage = await response.text();
        alert("삭제 실패: " + errorMessage);
      }
    } catch (error) {
      console.error("리뷰 삭제 중 오류:", error);
      alert("리뷰 삭제 중 오류가 발생했습니다.");
    } finally {
      // 오류 발생 시 버튼 상태 복원
      buttonElement.disabled = false;
      buttonElement.textContent = "삭제";
    }
  }
});
