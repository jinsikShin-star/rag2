# 검증 기록과 재현

## 확인된 기준

2026-11-03 게시 전 공개용 복사본에서 전체 오프라인 Maven `verify`를 한 번 실행해 **31개 테스트 통과, 실패·오류·스킵 0**, exit 0 / BUILD SUCCESS를 확인했다. 새 복사본에 build 출력이 없는 상태에서 main 31개와 test 7개, 총38소스를 실제 컴파일했다. Java 21.0.11 / Maven 3.10.0 / Spring Boot 4.1.2 환경이며 53.981초, 09:27:04 KST 종료다. 별도 환경에서 같은 결과를 얻으려면 아래 명령을 실제로 실행해야 한다.

| 테스트 클래스 | 수 | 범위 |
|---|---:|---|
| `DomainInvariantTest` | 2 | 식별자·버전 불변 조건과 현재 문서 판정 |
| `QuestionServiceTest` | 12 | 접근 범위, 현재/허용 근거, 무근거, 생성/관측 실패 |
| `AnswerControllerTest` | 7 | standalone MockMvc 입력·응답·타입·기본값·오류 계약 |
| `ExternalAdapterContractTest` | 1 | 외부 placeholder의 명시적 미구현 실패 |
| `DemoWiringTest` | 3 | 기본/demo 조립과 Boot mapper 설정 |
| `HexagonalArchitectureTest` | 5 | 운영 네 모듈 import, 코어 격리·의존성 방향·순환 |
| `SyntheticDatasetContractTest` | 1 | 합성 JSONL 6case/5문서의 label·인용 관계 |

게시 전 실행은 기존 캐시를 사용해 다운로드·설치 없이 완료했고 컴파일 설정 `--release 21 -Xlint:all -Werror`를 유지했다. JSONL 원본과 테스트 classpath 복사본도 바이트가 일치했다. 소스·테스트·설정·데이터는 원본과 같고 공개용 문서만 정리했다. 민감정보 패턴·로컬 경로·문서 링크와 미완료 GNN TODO 7단계를 확인했다. 이 점검은 운영 보안 감사가 아니다. test-only 모듈의 빈 JAR 경고는 main 코드가 없어서 발생한다.

이전 2026-11-02 원본의 전체 verify 기록은34.189초였고 그때 최종 컴파일 단계는 up-to-date였다. 그 이전 기록과 이번 새 복사본의 실제 컴파일을 구분한다.

## 재현 명령

Java 21 JDK가 필요하다. Wrapper가 Maven 3.10.0을 사용한다. 아래는 프로젝트 root에서 실행하는 portable 명령이다. 최초 실행은 공개 도구/의존성을 다운로드할 수 있으며 테스트 자체는 외부 RAG 서비스를 호출하지 않는다.

```powershell
.\mvnw.cmd -B -ntp verify
# 의존성이 같은 캐시에 모두 준비된 경우
.\mvnw.cmd -o -B -ntp verify
```

```sh
sh ./mvnw -B -ntp verify
```

## 재현 명령 — 실행 JAR

`rag-bootstrap-0.1.0-SNAPSHOT.jar`는 다른 모듈이 참조하는 plain JAR이고 `-exec.jar`만 실행 JAR이다. 기본 프로필은 비웹 모드로 조립 후 종료한다.

```powershell
java -jar .\rag-bootstrap\target\rag-bootstrap-0.1.0-SNAPSHOT-exec.jar
# 선택적 로컬 HTTP mock demo, 종료는 Ctrl+C
java -jar .\rag-bootstrap\target\rag-bootstrap-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=demo
```

기본 비웹 JAR 기동/종료는 이전 검증에서 확인했다. demo 신원은 고정이며 인증 구현이 아니다. 실제 HTTP 전체 요청부터 모델 응답까지의 통합 검증은 하지 않았다.

## 미실행·미구현

- 실제 Elasticsearch/PageIndex/외부 LLM/영구 저장 연결, 운영 인증/인가·보안 감사·취약점 스캔·부하/비용 검증
- 실제 RAG 품질 eval과 baseline 측정, 전체 JSON Schema 엔진
- Linux/macOS에서의 Wrapper 실행 검증, 이번 기록의 clean 빌드
- 온톨로지/KG/GNN 구현 및 학습: [백로그의 미완료 7단계](backlog.md)

테스트는 fixture/mock과 인프로세스 Spring/MockMvc를 사용한다. [mock 추적](mock-question-trace.md)과 [합성 case 계약](eval-case-contract.md)에서 소스·테스트·주장의 경계를 확인할 수 있다.
