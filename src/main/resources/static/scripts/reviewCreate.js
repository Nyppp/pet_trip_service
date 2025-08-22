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
    if (show) {
      contentEl?.focus();
    } else {
      // 모달 닫을 때 메모리 정리 및 수정 모드 초기화
      cleanupObjectUrls();
      if (imagesPreview) {
        imagesPreview.innerHTML = "";
      }
      updateImageUploadButton();
      resetModalToCreateMode();
    }
  };

  // 모달을 생성 모드로 초기화하는 함수
  function resetModalToCreateMode() {
    const modalTitle = document.getElementById("review-modal-title");
    const submitButton = document.getElementById("submit-review");

    if (modalTitle) modalTitle.textContent = "리뷰 작성";
    if (submitButton) submitButton.textContent = "등록";

    // 수정 모드 플래그 제거
    delete modal.dataset.editMode;
    delete modal.dataset.reviewId;
  }
  openBtn?.addEventListener("click", () => toggleModal(true));
  closeBtn?.addEventListener("click", () => toggleModal(false));
  cancelBtn?.addEventListener("click", () => toggleModal(false));
  modal?.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal-backdrop")) toggleModal(false);
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modal.classList.contains("hidden")) toggleModal(false);
  });

  // 페이지 나가기/새로고침 시 메모리 정리
  window.addEventListener("beforeunload", () => {
    cleanupObjectUrls();
  });

  // 페이지 숨김/표시 시 메모리 정리 (모바일 브라우저 대응)
  document.addEventListener("visibilitychange", () => {
    if (document.hidden) {
      cleanupObjectUrls();
      if (imagesPreview) {
        imagesPreview.innerHTML = "";
      }
      updateImageUploadButton();
    }
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

  // 다중 이미지 업로드 처리
  const imageUploadBtn = document.getElementById("image-upload-btn");
  const multipleImgInput = document.getElementById("multiple-img-input");
  const imagesPreview = document.getElementById("images-preview");

  let selectedFiles = []; // 선택된 파일들을 관리하는 배열
  const MAX_IMAGES = 5;
  let fileIdCounter = 0; // 파일별 고유 ID 생성용

  // 메모리 정리 함수 - 모든 Object URL 해제
  function cleanupObjectUrls() {
    selectedFiles.forEach((fileObj) => {
      if (fileObj.objectUrl) {
        try {
          URL.revokeObjectURL(fileObj.objectUrl);
        } catch (error) {
          console.warn("Object URL 해제 실패:", error);
        }
      }
    });
    selectedFiles = [];
    fileIdCounter = 0;
  }

  imageUploadBtn?.addEventListener("click", () => {
    multipleImgInput?.click();
  });

  multipleImgInput?.addEventListener("change", (e) => {
    const files = Array.from(e.target.files || []);

    // 기존 이미지 개수 계산
    const existingImageCount = imagesPreview.querySelectorAll('.image-preview-item[data-is-existing="true"]').length;

    // 현재 선택된 파일 수와 새로 추가할 파일 수 합계 확인
    const totalFiles = selectedFiles.length + files.length + existingImageCount;
    if (totalFiles > MAX_IMAGES) {
      alert(`최대 ${MAX_IMAGES}장까지만 선택할 수 있습니다. (기존: ${existingImageCount}장, 새로 추가: ${selectedFiles.length + files.length}장)`);
      e.target.value = ""; // 입력 초기화
      return;
    }

    // 각 파일에 대해 유효성 검사 및 추가
    files.forEach((file) => {
      if (!validateImageFile(file)) {
        return; // 유효하지 않은 파일은 건너뜀
      }

      // 중복 파일 체크 (파일명과 크기로 비교)
      const isDuplicate = selectedFiles.some((existingFileObj) => existingFileObj.file.name === file.name && existingFileObj.file.size === file.size);

      if (!isDuplicate) {
        // 파일에 고유 ID 부여
        const fileWithId = {
          file: file,
          id: fileIdCounter++,
          objectUrl: null, // Object URL을 저장할 필드
        };
        selectedFiles.push(fileWithId);
        addImagePreview(fileWithId);
      }
    });

    updateImageUploadButton();
    e.target.value = ""; // 다음 선택을 위해 입력 초기화
  });

  // 이미지 미리보기 추가 함수
  function addImagePreview(fileObj) {
    const previewItem = document.createElement("div");
    previewItem.className = "image-preview-item";
    previewItem.dataset.fileId = fileObj.id; // 고유 ID로 식별

    const img = document.createElement("img");
    const objectUrl = URL.createObjectURL(fileObj.file);
    fileObj.objectUrl = objectUrl; // Object URL 저장
    img.src = objectUrl;
    img.alt = "미리보기";

    const removeBtn = document.createElement("button");
    removeBtn.className = "image-remove-btn";
    removeBtn.innerHTML = "×";
    removeBtn.type = "button";

    // 고유 ID를 이용한 삭제 (클로저 문제 없음)
    removeBtn.addEventListener("click", () => removeImageById(fileObj.id));

    previewItem.appendChild(img);
    previewItem.appendChild(removeBtn);
    imagesPreview.appendChild(previewItem);
  }

  // ID 기반 이미지 제거 함수 (효율적!)
  function removeImageById(fileId) {
    // 배열에서 해당 ID의 파일 찾기
    const fileIndex = selectedFiles.findIndex((fileObj) => fileObj.id === fileId);

    if (fileIndex === -1) {
      console.error("File not found with ID:", fileId);
      return;
    }

    // 제거할 파일 객체 가져오기
    const fileToRemove = selectedFiles[fileIndex];

    // Object URL 해제 (메모리 누수 방지)
    if (fileToRemove.objectUrl) {
      URL.revokeObjectURL(fileToRemove.objectUrl);
    }

    // DOM에서 해당 미리보기 아이템 제거
    const previewItem = imagesPreview.querySelector(`[data-file-id="${fileId}"]`);
    if (previewItem) {
      previewItem.remove();
    }

    // 파일 배열에서 제거
    selectedFiles.splice(fileIndex, 1);

    // 버튼 상태만 업데이트 (재생성 없음!)
    updateImageUploadButton();
  }

  // 업로드 버튼 상태 업데이트
  function updateImageUploadButton() {
    const newImageCount = selectedFiles.length;
    const existingImageCount = imagesPreview.querySelectorAll('.image-preview-item[data-is-existing="true"]').length;
    const totalCount = newImageCount + existingImageCount;

    const btn = imageUploadBtn?.querySelector("span");
    if (btn) {
      if (totalCount >= MAX_IMAGES) {
        btn.textContent = `최대 ${MAX_IMAGES}장 선택됨`;
        imageUploadBtn.disabled = true;
        imageUploadBtn.style.opacity = "0.6";
        imageUploadBtn.style.cursor = "not-allowed";
      } else {
        btn.textContent = `+ 사진 추가 (${totalCount}/${MAX_IMAGES})`;
        imageUploadBtn.disabled = false;
        imageUploadBtn.style.opacity = "1";
        imageUploadBtn.style.cursor = "pointer";
      }
    }
  }

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

    // 선택된 이미지 파일들을 Base64로 변환
    let images = [];
    try {
      if (selectedFiles.length > 0) {
        // 이미지 업로드 중 메시지 표시
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = "이미지 처리 중...";

        // 모든 이미지를 Base64로 변환
        const base64Promises = selectedFiles.map((fileObj) => fileToBase64(fileObj.file));
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

    // 수정 모드인 경우 기존 이미지 정보 추가
    const isEditMode = modal.dataset.editMode === "true";
    if (isEditMode) {
      // 기존 이미지 URL들 수집
      const existingImages = [];
      const existingImageElements = imagesPreview.querySelectorAll('.image-preview-item[data-is-existing="true"]');
      existingImageElements.forEach((element) => {
        const imageUrl = element.dataset.imageUrl;
        if (imageUrl) {
          existingImages.push(imageUrl);
        }
      });

      payload.existingImages = existingImages;
      // images 필드는 그대로 유지 (Base64 이미지들)
    }

    try {
      // 수정 모드인지 확인
      const reviewId = modal.dataset.reviewId;

      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

      let res;
      if (isEditMode && reviewId) {
        // 수정 API 호출
        res = await fetch(`/api/places/${placeId}/reviews/${reviewId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest", [csrfHeader]: csrfToken },
          body: JSON.stringify(payload),
        });
      } else {
        // 생성 API 호출
        res = await fetch(`/api/places/${placeId}/reviews`, {
          method: "POST",
          headers: { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest", [csrfHeader]: csrfToken },
          body: JSON.stringify(payload),
        });
      }

      if (res.status === 401) {
        alert("로그인이 필요합니다.");
        return;
      }
      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        const errorMessage = errorData?.message || (isEditMode ? "리뷰 수정에 실패했습니다." : "리뷰 등록에 실패했습니다.");
        throw new Error(errorMessage);
      }

      await res.json();
      toggleModal(false);
      resetForm();
      alert(isEditMode ? "리뷰가 수정되었어요! 😊" : "리뷰가 등록되었어요! 😊");

      // 페이지 새로고침으로 변경사항 표시
      window.location.reload();
    } catch (err) {
      console.error(err);
      alert(err.message || "잠시 후 다시 시도해 주세요.");
    } finally {
      // 버튼 상태 복원
      const submitBtn = form.querySelector('button[type="submit"]');
      const isEditMode = modal.dataset.editMode === "true";
      submitBtn.disabled = false;
      submitBtn.textContent = isEditMode ? "수정" : "등록";
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

    // 다중 이미지 미리보기 초기화 (통합된 정리 함수 사용)
    cleanupObjectUrls();
    if (imagesPreview) {
      imagesPreview.innerHTML = "";
    }
    if (multipleImgInput) {
      multipleImgInput.value = "";
    }
    updateImageUploadButton();
  }
});
