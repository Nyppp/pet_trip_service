document.addEventListener("DOMContentLoaded", function () {
  fetch(`/api/places`)
    .then(res => res.json())
    .then(data => {
      allPlaces = Array.isArray(data) ? data : [];
      updateMarkersWithinBounds(); // 초기 렌더
    })
    .catch(err => console.error("장소 로드 실패:", err));
});

let map;
let allPlaces = [];
const markersById = new Map();

function initMap() {
  map = new google.maps.Map(document.getElementById("map"), {
    zoom: 15,
    disableDefaultUI: true
  });

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => {
        const center = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        map.setCenter(center);
        updateMarkersWithinBounds();
      },
      err => console.error("위치 불러오기 실패", err)
    );
  }

  map.addListener('idle', debounce(updateMarkersWithinBounds, 250));
}
initMap();

/* ------ 마커/카드 렌더링 ------ */

function updateMarkersWithinBounds() {
  if (!map || !allPlaces.length) return;
  const bounds = map.getBounds();
  if (!bounds) return;

  const visible = new Set();

  for (const p of allPlaces) {
    const lat = p.lat ?? p.latitude;
    const lng = p.lng ?? p.longitude;
    if (lat == null || lng == null) continue;

    const pos = new google.maps.LatLng(lat, lng);
    if (bounds.contains(pos)) {
      ensureMarker(p, pos);
      visible.add(p.id ?? `${lat},${lng}`);
    }
  }

  // 화면 밖 마커 제거
  for (const [id, m] of markersById) {
    if (!visible.has(id)) {
      m.setMap(null);
      markersById.delete(id);
    }
  }
}

function ensureMarker(place, position) {
  const id = place.id ?? `${position.lat()},${position.lng()}`;
  if (markersById.has(id)) return;

  const marker = new google.maps.Marker({
    map,
    position,
    title: place.name ?? ""
  });

  marker.addListener('click', () => {
    // 마커 클릭 → 패널 렌더
    showPlaceCard(place, marker);
  });

  markersById.set(id, marker);
}

/* ------ 카드 패널 ------ */

function showPlaceCard(place, marker) {
  if (marker) map.panTo(marker.getPosition());

  const img = (place.imageUrls && place.imageUrls.length)
    ? place.imageUrls[0]
    : '/images/no_image.png';

  const rating = place.rating ?? 0;
  const liked  = place.liked ?? 0;
  const category = place.categoryName ?? '';
  const addr = place.address ?? '';
  const name = place.name ?? '이름 없음';
  const detailHref = `/place/${place.id}`;

  const html = `
    <div class="place-card-wrap">
      <div class="close" onclick="hidePlaceCard()">✕</div>
      <div class="place-card">
        <img class="thumb" src="${img}" alt="${name}">
        <div class="body">
          <div class="title">${name}</div>
          <div class="category">${category}</div>
          <div class="addr">${addr}</div>
          <div class="meta">
            <span>⭐ ${rating.toFixed(1)}</span>
            <img src="/images/heart_filled.svg" alt="찜" width="16" height="16" /><span>${liked}</span>
          </div>
        </div>
        <div class="actions">
          <a class="btn" href="${detailHref}">상세보기</a>
        </div>
      </div>
    </div>
  `;

  const panel = document.getElementById('placeCardPanel');
  panel.innerHTML = html;
  panel.classList.remove('hidden');
}

function hidePlaceCard() {
  const panel = document.getElementById('placeCardPanel');
  panel.classList.add('hidden');
  panel.innerHTML = '';
}

/* ------ 유틸 ------ */
function debounce(fn, ms) { let t; return (...a)=>{ clearTimeout(t); t=setTimeout(()=>fn(...a), ms); }; }
