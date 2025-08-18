window.onload = function () {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        // 권한 허용 시 → 지도 페이지로 이동
        window.location.href = "/location/search";
      },
      (error) => {
        // 권한 거부 시 → 실패 안내 페이지로 이동
        alert("위치 권한이 거부되었습니다. 브라우저에서 허용 후 페이지를 새로고침 해주세요");
      }
    );
  } else {
    alert("브라우저가 위치 기능을 지원하지 않습니다. 위치 인증 서비스 사용이 불가합니다.");
    window.location.href = "/";
  }
};