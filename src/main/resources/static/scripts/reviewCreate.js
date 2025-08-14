document.addEventListener("DOMContentLoaded", () => {
  const openBtn = document.getElementById("open-review-modal");
  const modal = document.getElementById("review-modal");
  const closeBtn = document.getElementById("close-review-modal");
  const cancelBtn = document.getElementById("cancel-review");
  const form = document.getElementById("review-form");

  const starsWrap = document.getElementById("rating-stars");
  const contentEl = document.getElementById("review-content");
  const contentLen = document.getElementById("content-len");

  const petList = document.getElementById("pet-list");
  const addPetBtn = document.getElementById("add-pet");
  const petTemplate = document.getElementById("pet-item-template");

  const likeBtn = document.getElementById("like-btn");
  const placeId = likeBtn?.dataset.placeId || document.body?.dataset.placeId;

  const toggleModal = (show) => {
    if (!modal) return;
    modal.classList.toggle("hidden", !show);
    modal.setAttribute("aria-hidden", String(!show));
    if (show) contentEl?.focus();
  };
  openBtn?.addEventListener("click", () => toggleModal(true));
  closeBtn?.addEventListener("click", () => toggleModal(false));
  cancelBtn?.addEventListener("click", () => toggleModal(false));
  modal?.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal-backdrop")) toggleModal(false);
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modal.classList.contains("hidden")) toggleModal(false);
  });

  if (starsWrap) {
    const stars = Array.from(starsWrap.querySelectorAll(".star"));

    const updateStarsUI = (val) => {
      const full = Math.floor(val);
      const half = val - full >= 0.5 ? 1 : 0;
      stars.forEach((s, i) => {
        s.classList.remove("active", "half");
        if (i < full) s.classList.add("active");
        else if (i === full && half) s.classList.add("half");
      });
    };

    updateStarsUI(Number(starsWrap.dataset.value || 0));

    starsWrap.addEventListener("click", (e) => {
      const btn = e.target.closest(".star");
      if (!btn) return;
      const idx = stars.indexOf(btn); // 0 ~ 4
      const rect = btn.getBoundingClientRect();
      const x = e.clientX - rect.left; // 버튼 내부 클릭 X
      const half = x < rect.width / 2 ? 0.5 : 1; // 왼쪽=0.5, 오른쪽=1
      const val = idx + half; // 0.5 ~ 5.0
      starsWrap.dataset.value = String(val);
      updateStarsUI(val);
    });
  }

  contentEl?.addEventListener("input", () => {
    contentLen.textContent = String(contentEl.value.length);
  });

  document.querySelectorAll(".img-slot").forEach((slot) => {
    const input = slot.querySelector(".img-input");
    const preview = slot.querySelector(".img-preview");
    const placeholder = slot.querySelector(".img-placeholder");
    slot.addEventListener("click", () => input?.click());
    input?.addEventListener("change", () => {
      const file = input.files?.[0];
      if (!file) return;

      // 파일 유효성 검사
      if (!validateImageFile(file)) {
        input.value = ""; // 파일 선택 초기화
        return;
      }

      const url = URL.createObjectURL(file);
      preview.src = url;
      preview.hidden = false;
      placeholder.style.display = "none";
    });
  });

  // 이미지 파일 유효성 검사
  function validateImageFile(file) {
    // 파일 크기 검사 (5MB 이하)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      alert("이미지 파일 크기는 5MB 이하여야 합니다.");
      return false;
    }

    // 파일 타입 검증
    if (!file.type.startsWith("image/")) {
      alert("이미지 파일만 선택 가능합니다.");
      return false;
    }

    // 허용된 이미지 형식 검사
    const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"];
    if (!allowedTypes.includes(file.type)) {
      alert("JPG, PNG, GIF, WebP 형식의 이미지만 선택 가능합니다.");
      return false;
    }

    return true;
  }

  // 파일을 Base64로 변환하는 함수
  function fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
      reader.readAsDataURL(file);
    });
  }

  const MAX_PETS = Number(addPetBtn?.dataset.max || 5);

  if (petList && !petList.querySelector(".pet-item")) addPetRow();
  addPetBtn?.addEventListener("click", () => {
    const count = petList.querySelectorAll(".pet-item").length;
    if (count >= MAX_PETS) {
      alert(`반려동물 정보는 최대 ${MAX_PETS}개까지 입력할 수 있어요.`);
      return;
    }
    addPetRow();
  });

  function addPetRow() {
    if (!petTemplate || !petList) return;
    const node = petTemplate.content.firstElementChild.cloneNode(true);
    node.querySelector(".pet-remove").addEventListener("click", () => {
      const items = petList.querySelectorAll(".pet-item");
      if (items.length <= 1) {
        alert("최소 1개의 반려동물 정보가 필요해요.");
        return;
      }
      node.remove();
    });
    petList.appendChild(node);
  }

  form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!placeId) {
      alert("장소 정보가 없습니다.");
      return;
    }

    const rating = Number(starsWrap?.dataset.value || 0);
    const content = contentEl?.value.trim() || "";

    const pets = Array.from(petList.querySelectorAll(".pet-item")).map((item) => {
      const type = item.querySelector(".pet-type")?.value;
      const breed = item.querySelector(".pet-breed")?.value.trim() || null;
      const wStr = item.querySelector(".pet-weight")?.value;
      const weight = wStr ? parseFloat(wStr) : NaN;
      return { type, breed, weightKg: weight };
    });

    if (!rating) {
      alert("별점을 선택해 주세요.");
      return;
    }
    for (const p of pets) {
      if (!p.type || Number.isNaN(p.weightKg)) {
        alert("반려동물 종류와 무게(kg)를 입력해 주세요.");
        return;
      }
      if (p.weightKg < 0.1 || p.weightKg > 100) {
        alert("무게(kg)는 0.1 ~ 100 사이로 입력해 주세요.");
        return;
      }
    }

    // 이미지 파일들을 Base64로 변환
    let images = [];
    try {
      const imageInputs = document.querySelectorAll(".img-input");
      const imageFiles = Array.from(imageInputs)
        .map((input) => input.files?.[0])
        .filter((file) => file != null);

      if (imageFiles.length > 0) {
        // 이미지 업로드 중 메시지 표시
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = "이미지 처리 중...";

        // 모든 이미지를 Base64로 변환
        const base64Promises = imageFiles.map((file) => fileToBase64(file));
        images = await Promise.all(base64Promises);

        // 버튼 텍스트 복원
        submitBtn.textContent = "등록 중...";
      }
    } catch (error) {
      console.error("이미지 변환 실패:", error);
      alert("이미지 처리 중 오류가 발생했습니다.");

      // 버튼 상태 복원
      const submitBtn = form.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = "등록";
      }
      return;
    }

    const payload = {
      rating,
      content,
      petInfos: pets,
      images: images, // Base64 이미지 배열 추가
    };

    try {
      const res = await fetch(`/api/places/${placeId}/reviews`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest" },
        body: JSON.stringify(payload),
      });
      if (res.status === 401) {
        alert("로그인이 필요합니다.");
        return;
      }
      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        const errorMessage = errorData?.message || "리뷰 등록에 실패했습니다.";
        throw new Error(errorMessage);
      }

      await res.json();
      toggleModal(false);
      resetForm();
      alert("리뷰가 등록되었어요! 😊");

      // 페이지 새로고침으로 새 리뷰 표시
      window.location.reload();
    } catch (err) {
      console.error(err);
      alert(err.message || "잠시 후 다시 시도해 주세요.");
    } finally {
      // 버튼 상태 복원
      const submitBtn = form.querySelector('button[type="submit"]');
      submitBtn.disabled = false;
      submitBtn.textContent = "등록";
    }
  });

  // 폼 초기화 함수
  function resetForm() {
    form.reset();
    starsWrap.dataset.value = "0";
    starsWrap.querySelectorAll(".star").forEach((s) => s.classList.remove("active", "half"));
    contentLen.textContent = "0";
    petList.innerHTML = "";
    addPetRow();

    // 이미지 미리보기 초기화
    document.querySelectorAll(".img-preview").forEach((img) => {
      img.hidden = true;
      img.src = "";
    });
    document.querySelectorAll(".img-placeholder").forEach((p) => {
      p.style.display = "";
    });
    document.querySelectorAll(".img-input").forEach((input) => {
      input.value = "";
    });
  }
});
