document.addEventListener("DOMContentLoaded", () => {
  const input = document.getElementById("place_search");
  const list = document.getElementById("autocomplete_list");

  input.addEventListener("input", async () => {
    const keyword = input.value.trim();

    // 빈 입력은 무시
    if (keyword.length === 0) {
      list.innerHTML = "";
      return;
    }

    try {
      const res = await fetch(`/api/search?q=${encodeURIComponent(keyword)}`);
      const places = await res.json();

      list.innerHTML = "";
      places.forEach(place => {
        const li = document.createElement("li");
        li.textContent = place.name;
        li.addEventListener("click", () => {
          input.value = place.name;
          document.getElementById("place_id").value = place.id;
          list.innerHTML = "";
          // 선택된 place.id 같은 값은 hidden input에 따로 저장해도 좋음
        });
        list.appendChild(li);
      });
    } catch (err) {
      console.error("자동완성 실패:", err);
    }
  });
});
