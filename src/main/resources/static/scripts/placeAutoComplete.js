document.addEventListener("DOMContentLoaded", () => {
  const input = document.getElementById("place_search");
  const list = document.getElementById("autocomplete_list");

  input.addEventListener("input", async () => {
    const keyword = input.value.trim();

    // 빈 입력은 무시
    if (keyword.length === 0) {
      list.innerHTML = "";
      list.classList.remove("show");
      return;
    }

    try {
      const res = await fetch(`/api/search?q=${encodeURIComponent(keyword)}`);
      const places = await res.json();

      list.innerHTML = "";
      places.forEach((place) => {
        const li = document.createElement("li");
        li.textContent = place.name;
        li.addEventListener("mousedown", (e) => {
          e.preventDefault(); // blur 이벤트 방지
          input.value = place.name;
          document.getElementById("place_id").value = place.id;
          list.innerHTML = "";
          list.classList.remove("show");
        });
        list.appendChild(li);
      });

      // 검색 결과가 있으면 목록 표시
      if (places.length > 0) {
        list.classList.add("show");
      } else {
        list.classList.remove("show");
      }
    } catch (err) {
      console.error("자동완성 실패:", err);
      list.classList.remove("show");
    }
  });

  // 문서 클릭 시 목록 숨기기
  document.addEventListener("click", (e) => {
    if (!input.contains(e.target) && !list.contains(e.target)) {
      list.classList.remove("show");
    }
  });
});
