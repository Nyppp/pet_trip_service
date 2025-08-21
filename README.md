# 🐾 PetTrip: 반려동물 동반 여행 플랫폼

> 공공 데이터를 활용한 반려동물 여행지 추천 및 일정 플래너

---

## 1. 프로젝트 소개

### 📋 프로젝트 설명

PetTrip은 반려동물과 함께 방문 가능한 관광지를 추천하고, 여행 일정을 구성할 수 있는 웹 플랫폼입니다. 공공데이터포털의 반려동물 동반여행 서비스 API를 활용하여 반려동물 동반 여행에 특화된 서비스를 제공합니다.

### 📅 진행기간

- **2025년 7월 29일(화) ~ 2025년 8월 25일(월)**

### 🎯 주요기능

- **회원 관리**: 일반 회원가입/로그인, Google OAuth2 소셜 로그인
- **장소 검색**: 키워드, 카테고리, 위치 기반 관광지 검색
- **장소 상세**: 관광지 정보, 리뷰, 찜 기능, AI 요약
- **리뷰 시스템**: 별점 평가, 이미지 업로드, 반려동물 정보 포함
- **여행 플래너**: 일정 생성, 장소 추가, 시간 관리
- **AI 챗봇**: Alan AI 기반 상담 서비스
- **마이페이지**: 프로필 관리, 찜한 장소, 작성한 리뷰 관리

### 🔗 링크

- **노션 주소**: [프로젝트 노션](https://www.notion.so/Team-23f4c3b14a3580b5be54d98d2123a4b2)
- **배포 사이트**: [PetTrip 서비스](https://pet-trip-service.p-e.kr/)
- **시연 영상**: [YouTube 시연 영상]()
- **발표 자료**: [Google Slides 발표자료](https://docs.google.com/presentation/d/1Czaar7PO975W6uUUmfY18KhyrjKpEafz/edit?slide=id.p1#slide=id.p1)

---

## 2. 팀원 소개 및 역할

<table>
  <tr>
    <th colspan="4" style="text-align:center;">Team1 떠나개</th>
  </tr>
  <tr>
    <th style="text-align:center;">팀장 노윤표</th>
    <th style="text-align:center;">팀원 김성연</th>
    <th style="text-align:center;">팀원 권정연</th>
    <th style="text-align:center;">팀원 이재원</th>
  </tr>
  <tr>
   <td style="text-align:center;">
      <a href="https://github.com/Nyppp" target="_blank">
        <img src="https://github.com/Nyppp.png" alt="노윤표" height="120"/>
      </a>
    </td>
     <td style="text-align:center;">
      <a href="https://github.com/sungyeonkim27" target="_blank">
        <img src="https://github.com/sungyeonkim27.png" alt="김성연" height="120"/>
      </a>
    </td>
     <td style="text-align:center;">
      <a href="https://github.com/yeoni-2" target="_blank">
        <img src="https://github.com/yeoni-2.png" alt="권정연" height="120"/>
      </a>
    </td>
     <td style="text-align:center;">
      <a href="https://github.com/jwljwljwl" target="_blank">
        <img src="https://github.com/jwljwljwl.png" alt="이재원" height="120"/>
      </a>
    </td>
  </tr>
</table>

---

## 3. 사용 기술 및 도구

### 🛠️ Backend

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.4
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security, OAuth2
- **Build Tool**: Maven

### 🎨 Frontend

- **Template Engine**: Thymeleaf
- **CSS**: Custom CSS
- **JavaScript**: Vanilla JS

### 🗄️ Database & Storage

- **Database**: PostgreSQL
- **Image Storage**: AWS S3
- **Test Database**: H2

### 🔐 Authentication & API

- **OAuth2**: Google OAuth2
- **External APIs**:
  - 공공데이터포털 TourAPI 4.0
  - Google Maps API
- **AI Service**: Alan AI

### ☁️ Deployment & DevOps

- **Cloud**: AWS EC2
- **Version Control**: Git, GitHub
- **Documentation**: Swagger/OpenAPI

---

## 4. 디렉토리 구조

```
pet_trip_service/
├── src/
│   ├── main/
│   │   ├── java/com/oreumi/pet_trip_service/
│   │   │   ├── config/                 # 설정 클래스
│   │   │   │   ├── S3Config.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/             # 컨트롤러
│   │   │   │   ├── api/               # REST API 컨트롤러
│   │   │   │   ├── ChatController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── PlaceController.java
│   │   │   │   ├── ScheduleController.java
│   │   │   │   └── UserController.java
│   │   │   ├── DTO/                   # 데이터 전송 객체
│   │   │   │   ├── api/              # API 응답 DTO
│   │   │   │   ├── ChatDTO.java
│   │   │   │   ├── PlaceDTO.java
│   │   │   │   └── UserSignupDTO.java
│   │   │   ├── model/                 # 엔티티 모델
│   │   │   │   ├── Enum/             # 열거형
│   │   │   │   ├── User.java
│   │   │   │   ├── Place.java
│   │   │   │   ├── Review.java
│   │   │   │   └── Schedule.java
│   │   │   ├── repository/            # 데이터 접근 계층
│   │   │   ├── security/              # 보안 관련
│   │   │   └── service/               # 비즈니스 로직
│   │   └── resources/
│   │       ├── static/                # 정적 리소스
│   │       │   ├── css/              # 스타일시트
│   │       │   ├── images/           # 이미지 파일
│   │       │   └── scripts/          # JavaScript 파일
│   │       └── templates/             # Thymeleaf 템플릿
│   │           ├── common/           # 공통 템플릿
│   │           ├── main/             # 메인 페이지
│   │           ├── place/            # 장소 관련 페이지
│   │           ├── schedule/         # 일정 관련 페이지
│   │           └── user/             # 사용자 관련 페이지
│   └── test/                         # 테스트 코드
├── pom.xml                           # Maven 설정
└── README.md                         # 프로젝트 문서
```

---

## 5. ERD (Entity Relationship Diagram)
![ERD 설명](https://github.com/user-attachments/assets/8bb5c941-abd4-4d92-9b61-dd982e5a16ed)

---

## 6. 페이지 구성

### 📝 회원가입 페이지

![회원가입 페이지](https://github.com/user-attachments/assets/9936dff3-8239-4a43-9bba-5fdb0d12d677)

- 이메일, 비밀번호, 닉네임 입력
- 이메일 중복 확인
- 비밀번호 유효성 검사
- 닉네임 중복 확인

### 🔐 로그인 페이지

![로그인 페이지](https://github.com/user-attachments/assets/bdaee6cb-eade-4c7e-8046-b88821e1a8dd)

- 일반 로그인 (이메일/비밀번호)
- Google OAuth2 소셜 로그인
- 회원가입 페이지 링크

### 🏠 메인 페이지

![메인 페이지](https://github.com/user-attachments/assets/7dd4a574-9c7b-4ff2-bfcd-6b6b0e1d1b72)

- 인기있는 장소 추천(별점순)
- 키워드, 카테고리별 검색
- 지도 기반 시각화

### 🔍 장소 검색 페이지

![검색 페이지](https://github.com/user-attachments/assets/4ca1e6fd-255c-43a2-a5d4-b05dd6ce0146)


- 별점순, 좋아요순 등 정렬 기능
- 검색 결과의 지도 기반 시각화

### 📄 장소 상세 페이지

![장소 상세1](https://github.com/user-attachments/assets/d2f1a8de-554b-4d08-bfa5-666a272f4d8c)
![장소 상세2](https://github.com/user-attachments/assets/5e26b745-7c00-4dbb-b9a0-35f254ed49ee)

- 장소 정보 및 이미지
- 별점
- 찜 기능
- 스케쥴에 장소 추가
- AI 요약 정보
- 리뷰

### 📝 리뷰 작성 페이지

![리뷰 작성](https://github.com/user-attachments/assets/cb0667ab-2efa-43f3-b925-2bb286943873)

- 별점 평가
- 텍스트 리뷰
- 반려동물 정보 입력
- 이미지 업로드(최대 5장)
- 리뷰 수정 및 삭제

### 📅 여행 플래너

![스케쥴 생성 페이지](https://github.com/user-attachments/assets/2d985ce9-8668-4156-823c-6988558cb8d5)
![스케쥴 리스트 페이지](https://github.com/user-attachments/assets/870aba9b-87f8-4139-b853-68182c8fe810)
![스케쥴 장소 추가 페이지](https://github.com/user-attachments/assets/69405092-487a-460d-a8af-3f5e579aa394)
![스케쥴 장소 리스트 페이지](https://github.com/user-attachments/assets/23cd0919-892a-4ccc-aec4-fc98f6f76c5d)
![스케쥴 상세 페이지](https://github.com/user-attachments/assets/5b01a745-d178-4c06-80a2-554ca312694d)


- 일정 생성 및 관리
- 장소 추가 및 시간 설정
- 일정 수정 및 삭제

### 👤 마이페이지

![마이페이지(내정보)](https://github.com/user-attachments/assets/526e320e-c806-4af6-84a3-8b6b739ef6e7)
![마이페이지(찜한장소)](https://github.com/user-attachments/assets/d4352d97-ceda-4fb7-b2a1-be31a7ff4627)
![마이페이지(내가쓴리뷰)](https://github.com/user-attachments/assets/417597f7-fc72-4f72-8807-08fb95bba74e)

- 프로필 관리
- 찜한 장소 목록
- 작성한 리뷰

### 💬 AI 챗봇

![AI챗봇](https://github.com/user-attachments/assets/92322773-8fdf-4ed2-8611-849c6275814f)

- Alan AI 기반 채팅 기능
- 실시간 대화 기능
- 여행 정보 제공
- 채팅 초기화 기능
- 채팅 무한스크롤

---
 
## 🔧 Git 컨벤션


### 🌿 브랜치 전략

| 브랜치 이름 | 용도                           |
|-------------|--------------------------------|
| `main`      | 배포용 브랜치 (프로덕션)       |
| `develop`   | 통합 개발 브랜치               |
| `feature/*` | 기능 개발 브랜치               |
| `hotfix/*`  | 긴급 수정 브랜치               |

**예시 브랜치명**
- 로그인 기능: `feature/login`
- 스케쥴 기능: `feature/schedule`

**규칙**
- 브랜치명은 **소문자**로 통일합니다.
- 기능 브랜치는 `develop` 브랜치에 머지 후 **삭제**합니다.


### 📋 커밋 메세지

| 타입       | 설명                                  |
|------------|---------------------------------------|
| `feat`     | 새로운 기능 추가                      |
| `fix`      | 버그 수정                             |
| `docs`     | 문서 수정                             |
| `style`    | 코드 포맷팅, 세미콜론 누락 등         |
| `refactor` | 코드 리팩토링 (기능 변경 없음)         |
| `test`     | 테스트 코드 추가                      |

**커밋 예시**

```bash
# 기능 추가
feat: Google OAuth2 로그인 기능 구현

# 버그 수정
fix: 장소 검색 결과 정렬 오류 수정

# 문서 업데이트
docs: API 문서 업데이트

# 리팩토링
refactor: 리뷰 서비스 로직 개선
```

### 📄 파일명 컨벤션

| 구분              | 스타일         | 예시                       |
|-------------------|----------------|----------------------------|
| HTML              | 스네이크 케이스 | `mypage_likes.html`, `location_search.html` |
| CSS               | 카멜 케이스     | `scheduleCreate.css`, `locationSearch.css` |
| JavaScript        | 카멜 케이스     | `mypageLikes.js`, `scheduleItemList.js`   |
| Java (Spring)     | 파스칼 케이스   | `UserController.java`, `PlaceService.java` |
| DB                | 스네이크 케이스 | `schedule_item`, `review_img` |


