# Java/Spring RAG 헥사고날 포트폴리오

범용 문서 질의응답 백엔드의 **아키텍처·계약·mock·테스트용 프로젝트**입니다. 도메인과 유스케이스를 프레임워크/벤더 SDK에서 분리하고, 검색·생성·문서 메타데이터·접근 정책·관측성을 교체 가능한 포트로 구성합니다.

**실제 Elasticsearch·PageIndex·LLM·영구 저장·운영 인증은 미구현입니다.** demo는 고정 fixture와 mock을 사용합니다. 검색/생성 품질, 운영 보안 또는 배포 준비 완료를 주장하지 않습니다. 온톨로지·KG·GNN은 [미완료 TODO 7단계](docs/backlog.md)입니다.

## 현재 단계: 기획 검토 우선

[MES·제품 탄소 데이터 서비스 기획 검토본](docs/planning/README.md)을 먼저 검토합니다. 상용 서비스를 목표로 한 기획이며 제품 구현·출시·인증이 완료된 상태는 아닙니다. 기존 범용 RAG 코드는 유지하고, 기획 검토와 개발 범위 승인 후 필요한 부분만 코드에 매핑합니다.

## 모듈

```text
rag-domain/               순수 Java 값 객체와 불변 조건
rag-application/          질문 유스케이스, 입출력과 포트
rag-adapters/             HTTP, fixture/mock, 명시적 미구현 외부 어댑터
rag-bootstrap/            Spring 조립과 단일 실행 JAR
rag-architecture-tests/   의존성 경계와 합성 평가 데이터 계약 테스트
```

운영 네 모듈은 하나의 JVM으로 배포합니다. 검증 모듈은 운영 서비스가 아닙니다. domain/application에는 Spring·Jakarta·모델 SDK 의존성이 없습니다.

## 빌드와 실행

Java 21 JDK, Spring Boot 4.1.2, Maven 3.10.0(Wrapper 3.3.4)을 사용합니다. 버전과 선택 근거는 [아키텍처](docs/architecture.md)에 기록했습니다. 최초 빌드는 Maven과 공개 의존성을 다운로드하며, 테스트 자체는 외부 검색·모델을 호출하지 않습니다.

```powershell
.\mvnw.cmd -B verify
# 의존성이 같은 캐시에 이미 있을 때
.\mvnw.cmd -o -B verify
```

```sh
sh ./mvnw -B verify
```

기본 실행은 비웹 모드로 조립 후 종료합니다. HTTP demo는 명시적으로 프로필을 선택해야 하며 기본 주소는 loopback입니다.

```powershell
java -jar .\rag-bootstrap\target\rag-bootstrap-0.1.0-SNAPSHOT-exec.jar
# 선택적 로컬 mock HTTP demo. 종료: Ctrl+C
java -jar .\rag-bootstrap\target\rag-bootstrap-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=demo
```

```http
POST /api/v1/answers
Content-Type: application/json

{"question":"fixture question","topK":5}
```

질문은 1~4000자의 공백이 아닌 문자열, topK는 1~20의 JSON 정수입니다. topK 생략/null은 5입니다. demo 호출자는 서버에서 `demo-tenant/demo-user`로 고정하며 운영 인증이 아닙니다. 결과는 `MOCK_ANSWER/MOCK` 또는 `NO_CONTEXT/NONE`으로 구분합니다. `.env.example`은 비밀 없는 설명용이며 자동 로드하지 않습니다.

## 검증과 읽는 순서

2026-11-03 게시 전 공개용 복사본에서 소스 38개를 새로 컴파일하고 전체 오프라인 `verify`로 **31개 테스트 통과, 실패·오류·스킵 0**을 확인했습니다. 재현 범위와 한계는 [검증 기록](docs/verification.md)을 참고하세요.

1. [아키텍처·포트와 선택 근거](docs/architecture.md)
2. [mock 질문의 소스·포트·테스트 추적](docs/mock-question-trace.md)
3. [API 계약](docs/api-contract.yaml)과 [권한·문서 변경 확장](docs/security-and-lifecycle.md)
4. [합성 case 로더 계약](docs/eval-case-contract.md), [평가 설계](eval/README.md)
5. [개발 기록 요약](docs/weekly-review.md), [백로그와 GNN TODO](docs/backlog.md)

문서 쓰기 API는 계획 항목이며 handler가 없습니다. 외부 어댑터 placeholder는 명시적 미구현 예외를 던지고 demo에 연결하지 않습니다. 코드·fixture는 소스 전달용이며 빌드 출력, 로컬 캐시, 인증정보를 포함하지 않습니다. [Maven Wrapper 제3자 고지](docs/third-party/README.md)를 보존했습니다.
