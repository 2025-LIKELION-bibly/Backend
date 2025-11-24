<img src="https://i.postimg.cc/gjvfGTQY/hwamyeon-kaebcheo-2025-11-24-233805.jpg">

### **✏️ 매번 만나서 주고받기 번거롭던 교환독서 ✏️** 
<p>
&nbsp; 사람들과 서로 인상 깊은 구절들을 공유하며 책을 함께 읽고 싶은
<br>
모든 책러버들을 위한 서비스 <b>"비블리"</b>
<br><br>
직접 만나지 않고도 여러 사람들과 교환독서를 즐길 수 있다면 어떨까요?
<br>
<b>비블리</b>는 시공간의 제약 없이 모두 함께 책을 읽을 수 있는 교환독서 플랫폼이에요.
<br><br>
모임원들과 서로 인상 깊은 구절을 공유하거나 이야기할 수 있고
<br>요즘 어떤 책이 인기인지, 남들은 이 책을 어떻게 읽고 있는지 알 수 있어요.
<br><br>
혼자서만 읽고 또 읽는 게 지루하진 않았나요?
<br>
이제, <b>Bibly</b>로 함께 읽는 재미에 흠뻑 빠져보세요!
</p>

<br>

## Contributors✏️
|                             유수빈<br/>([@b1nnnnid](https://github.com/b1nnnnid))                          |                            이수진<br/>([@leewatertrue](https://github.com/leewatertrue))                             |
|:--------------------------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------: | 
| <img width="200px" height="200px" src="https://avatars.githubusercontent.com/u/173851812?s=400&v=4"/> | <img width="200px" height="200px" src="https://avatars.githubusercontent.com/u/181321387?v=4"/> | 
|                                                  BE                                                   |                                                     BE                                                     |   
|     `ERD 설계`<br/>`책장 로직`<br/>`읽기 세션 관리`<br/>`흔적 남기기`<br/>`디폴트 홈`<br/>`DB 관리`      |     `ERD 설계`<br/>`사용자 관리`<br/>`모임 관리`<br/>`교환 로직` <br/>`책 고르기`<br/>`배포`    | 

<br>

## API Docs✏️
[🌿 스웨거 링크 (서버 배포)](http://bib-ly.kro.kr/swagger-ui/index.html) <br>
[☘️ 문서 스웨거 (준비 예정)](http://bib-ly.kro.kr/swagger-ui/index.html)<br>
[📝 API 명세서](https://www.notion.so/API-296830fd62a581adab37c64aaf59d810) <br>
<br>


## Tech Stack✏️

<table width="100%">
<tr>
<th align="center">Backend</th>
<td align="left">
<img height="50" src="https://user-images.githubusercontent.com/25181517/117201156-9a724800-adec-11eb-9a9d-3cd0f67da4bc.png">  
<img height="50" src="https://user-images.githubusercontent.com/25181517/183891303-41f257f8-6b3d-487c-aa56-c497b880d0fb.png">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/intellij.png">
<img height="50" src="https://i.postimg.cc/3xnZV03R/sunny.png">
</td>
</tr>
<tr>
<th align="center">Database</th>
<td align="left">
<img height="50" src="https://user-images.githubusercontent.com/25181517/183896128-ec99105a-ec1a-4d85-b08b-1aa1620b2046.png"> 
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/postman.png">
</td>
</tr>
<tr>
<th align="center">CI/CD</th>
<td align="left">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/git.png">
</td>
</tr>
<tr>
<th align="center">Deployment</th>
<td align="left">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/docker.png">
<img height="50" src="https://user-images.githubusercontent.com/25181517/183896132-54262f2e-6d98-41e3-8888-e40ab5a17326.png">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/swagger.png">
</td>
<tr>
<th align="center">collaboration</th>
<td align="left">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/github.png">
<img height="50" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/figma.png">
<img height="50" src="https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white">
</td>
</tr>
  
</table>
<br>

## Development Architecture☘✏️


<br>

## Branch Strategy✏️

```
- main (배포용)
- feat/#이슈번호 (작업용)
```

## Code Codevention✏️
- **클래스/인터페이스: Pascal**
UserController
UserService
- **함수(메서드), 변수명: Camel**
getUserByID()
createUser()
userName
- **상수: UPPER_Snake**
MAX_LENGTH
- **패키지: 전부 소문자, 언더바×**
com.example.api.user<br><br>

## 커밋 및 이슈✏️
| 커밋 유형  | 의미                                                                                  |
| ---------- | ------------------------------------------------------------------------------------- |
| 🎉 `Initial`     | 초기 세팅시에만 사용                             |
| ✨ `Feat`     | 새로운 기능 추가                             |
| 🐛 `Fix`      | 버그 수정                   |
| 📝 `Docs`     | 문서(README, SWAGGER 등) 수정                                                          |
| ♻️ `Refactor` | 코드 리팩토링(기능 변화 없이 코드 구조 개선)       |
| 🌏 `Deploy`    | 배포 설정                    |
| 💚 `CI/CD`    | CI/CD 관련 설정 수정                    |
| ⚙️ `Setting`    | 그 외 설정 수정                    |
| 🚀 `Chore`    | 그 외 기타 수정 및 잡일성 작업                    |

<br>

## Project Structure️️✏️

```
.
├── src
│   └── main
│       └── java
│           └── com
│               └── app
│                   ├── global            # (공통) 전역 기능 및 설정
│                   │   ├── auth          
│                   │   ├── common        
│                   │   ├── config        
│                   │   ├── controller    
│                   │   ├── exception     
│                   │   └── util          
│                   ├── addgroup          # 그룹 추가
│                   │   └── entity
│                   ├── assignment        # 읽기 할당
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── book              # 책
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── bookmark          # 북마크
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── bookshelf         # 책장
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── enums
│                   │   ├── repository
│                   │   └── dto
│                   ├── comment           # 댓글
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── enums
│                   │   ├── repository
│                   │   └── dto
│                   ├── group             # 모임
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── enums
│                   │   ├── repository
│                   │   ├── dto
│                   │   └── util
│                   ├── highlight         # 하이라이트
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── home              # 메인 홈
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── invite            # 초대
│                   │   └── entity
│                   ├── member            # 멤버
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── enums
│                   │   ├── repository
│                   │   ├── dto
│                   │   └── util
│                   ├── navigator         # 네비게이터
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── enums
│                   │   └── repository
│                   ├── page              # 페이지
│                   │   ├── entity
│                   │   └── repository
│                   ├── progress          # 진행도
│                   │   ├── entity
│                   │   └── repository
│                   ├── session           # 세션
│                   │   ├── controller
│                   │   ├── service
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── dto
│                   ├── timetest          # 시간 테스트
│                   │   ├── controller
│                   │   └── service
│                   └── user              # 사용자
│                       ├── controller
│                       ├── service
│                       ├── entity
│                       ├── enums
│                       ├── repository
│                       └── dto
├── src
│   └── main
│       └── resources  # 환경 설정 파일 
│           ├── application.yml
│           ├── application-local.yml
│           └── application-prod.yml
└── build.gradle
```
