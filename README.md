# Runnect-Spring-Boot-Server


### 점과 점으로, 코스와 코스로 연결되는 너와 나의 러닝 경험
![표지](https://user-images.githubusercontent.com/88873302/212262655-0f14bae8-79d9-4aff-b52a-993694727a6a.jpg)
</br>
![A333](https://user-images.githubusercontent.com/88873302/212286726-608ec06a-5631-4aaf-ae7f-bf711a4ef234.jpg)

</aside>
<hr>
</br>

# ☝ 서비스 핵심 기능
### 1. 코스 그리기
> 코스 그리기로 달리기 전 목표를 설정하고 실시간 트래킹으로 코스르 따라 잘 달리고 있는지 확인합니다.
### 2. 코스 발견
> 코스 발견을 통해 나에게 맞는 코스를 추천 받거나 다른 유저가 공유한 코스를 검색하고 스크랩합니다. 코스를 직접 업로드할 수 도 있습니다.
### 3. 코스 보관함
> 코스 보관함에서 내가 그린 코스와 스크랩 코스를 관리합니다.
### 4. 마이페이지
> 마이페이지에서 프로필과 활동 기록, 업로드한 코스를 확인하고 목표 보상으로 동기를 강화합니다.

</aside>
<hr>
</br>


# 🧑‍🔧 Tech Stack
### Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) <img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logoColor=white">  <img src="https://img.shields.io/badge/hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"> 

### DB
<img src="https://img.shields.io/badge/amazon rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/postGIS-4169E1?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/amazon s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">

### CI/CD
<img src="https://img.shields.io/badge/aws code deploy-FF9900?style=for-the-badge&logoColor=white">

### Deploy
<img src="https://img.shields.io/badge/amazon ec2-FF9900?style=for-the-badge&logo=amazon ec2&logoColor=white"> <img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> 

 
### Develop Tool
<img src="https://img.shields.io/badge/intelliJ-000000?style=for-the-badge&logo=intellij idea&logoColor=white"> <img src="https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white"> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"> 

### Communicate Tool
<img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
<br> 
<br>


</aside>
<hr>
</br>



# 🏠 server architecture
![image](https://github.com/Runnect/Runnect-Spring-Boot-Server/assets/65851554/4cbb080d-f715-41da-bd2a-10256ecd9023)


</aside>
<hr>
</br>

# 💽 DB ERD
<img width="545" alt="스크린샷 2024-03-26 오후 1 15 33" src="https://github.com/Runnect/Runnect-Spring-Boot-Server/assets/65851554/c48b0966-e73d-4ec0-be08-b097c6890265">

</aside>
<hr>
</br>

# 📄 API(+ Non - API) Docs


<img width="1351" alt="스크린샷 2024-04-05 오후 6 35 31" src="https://github.com/Runnect/Runnect-Spring-Boot-Server/assets/65851554/6cdf531e-4945-4ace-86ad-a3feecd68793">
<img width="1351" alt="스크린샷 2024-04-05 오후 6 36 54" src="https://github.com/Runnect/Runnect-Spring-Boot-Server/assets/65851554/48425786-0d44-4f66-90ab-d40453b32fb8">
<img width="1351" alt="스크린샷 2024-04-05 오후 6 36 14" src="https://github.com/Runnect/Runnect-Spring-Boot-Server/assets/65851554/c3a009d2-6bb5-4af0-885d-7ba5ac0c2e7a">

</aside>
<hr>
</br>


# 🙆‍♀️ 역할분담 

|담당자|담당 내용|
|:---|:---|
|유수화|EC2, publicCourse & stamp 관련 api, 인프라 구축|
|전선희|RDS, course & user 관련 api, 인프라 구축|
|박수린|S3, record & scrap 관련 api, 인프라 구축|


</aside>
<hr>
</br>

# 🗣️️ 컨벤션

> 💡 **동료들과 말투를 통일하기 위해 컨벤션을 지정합니다.**
> 오합지졸의 코드가 아닌, **한 사람이 짠 것같은 코드**를 작성하는 것이 추후 유지보수나 협업에서 도움이 됩니다. 내가 코드를 생각하면서 짤 수 있도록 해주는 룰이라고 생각해도 좋습니다!

## 👩‍💻 Coding Conventions

<details>
<summary>명명규칙(Naming Conventions)</summary>
<div markdown="1">

1. 이름으로부터 의도가 읽혀질 수 있게 쓴다.

2. 단수를 기본형으로 한다.

   - 기능 자체에서 단수, 복수를 구분하는 경우에만 복수 사용 ex. 다중삭제, 단일삭제

3. DB의 테이블, 클래스에는 `PascalCase`를 사용한다.

4. 변수, 메소드에는 `camelCase`를 사용한다.

5. DB의 테이블의 칼럼에는 `snake_case`를 사용한다.

6. 상수, enum에는 `UPPER_SNAKE_CASE`를 사용한다.

7. 메소드는 `crud + http method`(동사) + 명사 형태로 작성한다.

   - c : ex. `createUser`
   - r : ex. `getUser`
   - u : ex. `updateUser`
   - d : ex. `deleteUser`

8. 약어 사용은 최대한 지양한다.

9. 이름에 네 단어 이상이 들어가면 팀원과 상의를 거친 후 사용한다.
   </div>
   </details>

<details>
<summary>주석(Comment)</summary>
<div markdown="1">

1. 해당 메소드가 어디에 쓰이는지 설명한다.

2. 해당 분기문이 어떤 분기인지 설명한다.

3. 반복문에서 어떤 조건에서 반복되는지 설명한다.

4. 정렬하고 필터링할때 어떤 조건의 정렬과 필터링인지 설명한다.

</div>
</details>

<hr>
</br>

## 🌳 Branch

🌱 Trunk-Based Development

`main branch` : 유일한 trunk. 항상 배포 가능한 상태를 유지하며, 운영 서버와 staging 서버 모두 이 branch에서 배포된다.

`feature branch`: 각 작업 단위의 짧은 수명 branch. `main`에서 분기해 하루~수일 내에 PR로 병합한다.

- `main`에서 분기, `feat/#issue번호-작업요약` 형식으로 branch 생성
  - 브랜치명이 `test/...`로 시작하면 GitHub ref 경로 충돌이 나므로 테스트 관련 branch는 `tests/...`(복수형) 사용
- 작업 완료 후 PR 생성 (base: `main`)
  - CI(`build` status check)를 통과해야 병합 가능
  - 미완성 기능은 브랜치를 오래 살려두지 말고, feature flag로 감싸 `main`에 빠르게 합친다
- 리뷰 완료 후 squash merge, 병합된 branch는 삭제

과거에는 `dev`/`main` 두 개의 장수 branch를 cherry-pick으로 동기화하는 방식이었으나, 두 branch가 내용상 계속 어긋나는 문제(cherry-pick hell)가 반복되어 폐기했다. staging 배포도 `main` push를 기준으로 트리거된다.

</aside>
<hr>
</br>

## 🧵 Commit Convention

<aside>
📍  git commit message convention

`ex) feat(변경한 파일) : 변경 내용 (/#issue num)`

```plain
- ✨ feat:      새로운 기능 구현
- 🐛 fix:       버그, 오류 해결
- 🧹 chore:     src 또는 test 파일을 수정하지 않는 기타 변경 사항 ( 새로운 파일 생성, 파일 이동, 이름 변경 등 )
- ♻️ refactor:  버그 수정이나 기능 추가가 없는 코드 변경 ( 코드 구조 변경 등의 리팩토링 )
- 💎 style:     코드의 의미에 영향을 미치지 않는 변경 사항 ( 코드 형식, 세미콜론 추가: 비즈니스 로직에 변경 없음 )
- 🏗️ build:    빌드 시스템 또는 외부에 영향을 미치는 변경 사항 종속성 ( 라이브러리 추가 등 )
- 📈 perf:      성능을 향상 시키기 위한 코드 변경
- 🧪 test:      테스트 추가 또는 이전 테스트 수정
- 📝 docs:      README나 WIKI 등의 문서 개정
- ⏪️ revert:    이전 커밋을 되돌리는 경우
- 📦 ci:      CI 구성 파일 및 스크립트 변경
- Merge: 다른브렌치를 merge하는 경우
- Init : Initial commit을 하는 경우
```

