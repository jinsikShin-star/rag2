# 제3자 코드 고지

`mvnw`, `mvnw.cmd`는 Apache Maven Wrapper Plugin 3.3.4가 생성한 공식 only-script 배포본이다. 스크립트의 원래 저작권·Apache License 2.0 헤더를 보존했다. [라이선스 본문](MAVEN-WRAPPER-LICENSE.txt)은 Wrapper에 적용하며 프로젝트 자체의 배포 라이선스를 정한 것은 아니다.

Wrapper는 Maven 3.10.0을 공식 Maven Central URL에서 받도록 고정했다. `distributionSha256Sum`은 Apache 공식 SHA-512와 일치한 바이너리 ZIP의 SHA-256이다.

- SHA-512: `22d31676d5b92ed53308c19ecded3245d0efd0cddfec51ec7bab62f4f13ad70ddddceff8e62940e0e8e2421f3cde8106737f4becec0aa034ffeff2a754e88337`
- SHA-256: `1f6d9909266510f039f59aa0e13dcd2c66da85f043e56276e41f2918f8bddaff`
- [Apache 공식 Maven 다운로드](https://maven.apache.org/download.cgi)
- [Apache 공식 Wrapper 문서](https://maven.apache.org/tools/wrapper/)

소스 ZIP에는 Maven/JDK/라이브러리 바이너리나 캐시를 넣지 않는다. 향후 바이너리를 재배포하면 각 의존성의 라이선스·NOTICE도 별도로 검토해야 한다.
