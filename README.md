<h2>📚교환독서 플랫폼 Bibly📚</h2>

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
-노션링크자리-
<br>

## Contributors✏️
|                             유수빈<br/>([@b1nnnnid](https://github.com/b1nnnnid))                          |                            이수진<br/>([@leewatertrue](https://github.com/leewatertrue))                             |
|:--------------------------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------: | 
| <img width="200px" height="200px" src="https://avatars.githubusercontent.com/u/173851812?s=400&v=4"/> | <img width="200px" height="200px" src="https://avatars.githubusercontent.com/u/181321387?v=4"/> | 
|                                                  BE                                                   |                                                     BE                                                     |   
|     `ERD 설계`<br/>`책장 로직`<br/>`읽기 세션 관리`<br/>`흔적 남기기`<br/>`디폴트 홈`<br/>`DB 관리`      |     `ERD 설계`<br/>`사용자 관리`<br/>`모임 관리`<br/>`교환 로직` <br/>`책 고르기`<br/>`배포`    | 

<br>

## API Docs✏️
http://localhost:8080/swagger-ui/index.html#/ <br>
https://www.notion.so/API-296830fd62a581adab37c64aaf59d810


## Tech Stack✏️

![Figma](https://img.shields.io/badge/figma-E0474C?style=for-the-badge&logo=figma&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Github](https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white)
<br>

![TypeScript](https://img.shields.io/badge/typescript-%233178C6.svg?style=for-the-badge&logo=typescript&logoColor=white)
![React](https://img.shields.io/badge/react-%2361DAFB.svg?style=for-the-badge&logo=react&logoColor=black)
<br>

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
<br>
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

<br>

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

<br>

## Development Architecture☘✏️


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
com.example.api.user
