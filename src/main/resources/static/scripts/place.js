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
                if (e.key === "ArrowRight") { e.preventDefault(); nextSlide(); }
                else if (e.key === "ArrowLeft") { e.preventDefault(); prevSlide(); }
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

    const aiWrap   = document.querySelector(".ai-split");
    const pid      = aiWrap?.dataset?.pid;            // place.html에서 <section class="ai-split" th:attr="data-pid=${place.id}">
    const reviewEl = document.getElementById("ai-review");
    const petEl    = document.getElementById("ai-pet");
    const btn      = document.getElementById("ai-update-btn"); // place.html에 항상 노출하는 버튼

    if (btn && pid) {
        btn.addEventListener("click", async () => {
            btn.disabled = true;
            btn.textContent = "업데이트 중...";          // 엄마표 느낌으로 차분하게 :)

            try {
                const res = await fetch(`/place/${pid}/ai/summary?force=true`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" }
                });
                if (!res.ok) throw new Error("response not ok");

                const data = await res.json();

                const rv = (data.aiReview || "").trim();
                const pt = (data.aiPet || "").trim();

                if (reviewEl) reviewEl.textContent = rv || "생성된 요약이 없습니다.";
                if (petEl)    petEl.textContent    = pt || "생성된 정보가 없습니다.";

            } catch (e) {
                alert("AI 요약 업데이트에 실패했어요. 잠시 후 다시 시도해 주세요.");
            } finally {
                btn.disabled = false;
                btn.textContent = "AI 요약 업데이트";
            }
        });
    }
});
