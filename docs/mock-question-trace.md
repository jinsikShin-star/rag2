# mock 질문 한 건의 소스·포트·테스트 추적

확인일: **2026-10-30 금요일**. 오늘 변경은 이 추적표와 첫 주 리뷰를 연결하는 문서 작업이다. 제품 코드·테스트·설정·의존성을 추가하거나 바꾸지 않았다.

## 따라갈 입력과 결과

명시적 `demo` 프로필에 다음 요청이 들어온다고 가정해 현재 소스를 따라간다. 기본 프로필은 비웹이며 demo API를 켜지 않는다.

```http
POST /api/v1/answers
Content-Type: application/json

{"question":"fixture question","topK":5}
```

호출자는 bootstrap의 `Actor("demo-tenant", "demo-user")`로 고정된다. HTTP body/header에서 신원을 만들지 않는다. 이 고정 정책은 운영 인증이 아니다.

아래 JSON은 **현재 소스에서 도출한 예상 예시**다. 실제 HTTP 전체 경로를 호출해 수집한 응답이나 새로 추가한 통합 테스트의 결과가 아니다.

```json
{
  "outcome": "MOCK_ANSWER",
  "answer": "[MOCK] Fixed local demonstration; no LLM was called.",
  "citations": [{"documentId":"demo-document","version":1,"locator":"fixture:section-1"}],
  "generationMode": "MOCK"
}
```

## 호출·데이터 추적표

| 순서 | 실제 소스와 계약 | 전달/확인하는 값 | 연결해서 읽을 테스트 |
|---|---|---|---|
| 0 | [DemoConfiguration](../rag-bootstrap/src/main/java/io/github/ragportfolio/bootstrap/DemoConfiguration.java)의 `demoActor()` / `questions()` | `demo`에서만 고정 Actor와 fixture 검색·catalog·정책·mock 생성·관측 adapter를 `QuestionService`에 주입 | `DemoWiringTest.defaultProfileDoesNotWireDemoApiOrMockUseCase`, `explicitDemoProfileWiresOnlyLocalFixtureFlowWithoutSockets` |
| 1 | [AnswerController.answer](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/http/AnswerController.java), [StrictJsonTypes](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/http/StrictJsonTypes.java) | JSON 질문 문자열과 topK 정수 검증. `topK` 생략/null이면 5. 고정 Actor로 명령 생성 | `AnswerControllerTest.mapsValidatedRequestAndIgnoresUntrustedIdentityHeaders`, `rejectsJsonTypesThatDoNotMatchTheContract`; Boot mapper 적용은 `DemoWiringTest.bootMapperAppliesTheSameStrictJsonTypesAsStandaloneHttpTests` |
| 2 | [AskQuestionCommand](../rag-application/src/main/java/io/github/ragportfolio/application/AskQuestionCommand.java) → [AskQuestionUseCase.ask](../rag-application/src/main/java/io/github/ragportfolio/application/AskQuestionUseCase.java) → [QuestionService.ask](../rag-application/src/main/java/io/github/ragportfolio/application/QuestionService.java) | Actor 필수, 질문 공백/4000자 초과 거부, topK 1~20. 이 예시는 Actor/demo 질문/5를 보존 | `QuestionServiceTest.validatesQuestionAndLimitsWithoutFrameworkDependencies` |
| 3 | [AccessPolicyPort.scopeFor](../rag-application/src/main/java/io/github/ragportfolio/application/port/AccessPolicyPort.java) → [DemoAccessPolicy](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/stub/DemoAccessPolicy.java) | `SearchScope("demo-tenant", {"demo-document"})`. 유스케이스가 Actor와 scope의 테넌트 일치를 검사 | `passesScopeAndOnlyCurrentAuthorizedEvidenceToGeneration`, `wrongTenantPolicyFailsClosedBeforeRetrieval` |
| 4 | [RetrieveEvidencePort.retrieve](../rag-application/src/main/java/io/github/ragportfolio/application/port/RetrieveEvidencePort.java) → [FixtureRetrievalAdapter](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/stub/FixtureRetrievalAdapter.java) | `(question, 5, scope)`를 받음. tenant/docID가 scope에 있으면 고정 Evidence 1개 반환 | 정상 demo 조립 테스트와 application의 scope 전달 검사. 실제 검색 관련도 검증은 없음 |
| 5 | [DocumentCatalogPort.findCurrent](../rag-application/src/main/java/io/github/ragportfolio/application/port/DocumentCatalogPort.java) → [FixtureDocumentCatalog](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/stub/FixtureDocumentCatalog.java), [Document.isCurrent](../rag-domain/src/main/java/io/github/ragportfolio/domain/Document.java), `AccessPolicyPort.canRead` | 후보의 tenant/docID가 scope 안인지 확인한 후 catalog의 `demo-document / demo-tenant / v1 / ACTIVE`와 비교하고 현재 권한을 재확인. 허용 근거를 topK까지 수집 | application 정상 테스트는 오래된 버전·삭제·권한 취소·미존재·다른 테넌트·범위 밖 후보가 생성 입력에서 빠지는지 별도 synthetic 값으로 확인 |
| 6 | [GenerateAnswerPort.generate](../rag-application/src/main/java/io/github/ragportfolio/application/port/GenerateAnswerPort.java) → [MockGenerationAdapter](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/stub/MockGenerationAdapter.java) | `(question, authorizedEvidence 1개)` → 고정 답변 문자열과 `GeneratedAnswer.Mode.MOCK` | demo 조립 테스트의 mode 확인; application 정상 테스트의 전달 근거/불변 목록 확인 |
| 7 | `QuestionService` → [Citation](../rag-application/src/main/java/io/github/ragportfolio/application/Citation.java) / [AnswerResult](../rag-application/src/main/java/io/github/ragportfolio/application/AnswerResult.java) | 허용된 Evidence에서만 `Citation(demo-document,1,fixture:section-1)` 구성. 결과 `MOCK_ANSWER / MOCK` | demo 조립 테스트는 citation의 docID, application 정상 테스트는 별도 fixture의 정확한 인용 목록을 확인 |
| 8 | finally의 [ObservationPort.completed](../rag-application/src/main/java/io/github/ragportfolio/application/port/ObservationPort.java) → [LoggingObservationAdapter](../rag-adapters/src/main/java/io/github/ragportfolio/adapters/observability/LoggingObservationAdapter.java) | `Outcome.ANSWER`, 실행마다 다른 elapsedNanos, evidenceCount=1. 질의·근거 원문은 이 로그 인자에 없음 | `observationFailureDoesNotChangeSuccessfulResult`, `generationFailureKeepsCauseAndObservationCannotMaskIt` |

실제 fixture 근거는 `(documentId=demo-document, tenantId=demo-tenant, version=1, locator=fixture:section-1)`이며 본문은 `Synthetic local fixture for the hexagonal RAG architecture skeleton.`이다. catalog는 현재 메타데이터 조회용이다. 검색 adapter는 질문의 관련도를 무시하는 고정 fixture이고, 생성 adapter는 질문/근거를 요약하지 않고 같은 문자열을 돌려준다. 실제 검색·LLM 품질이나 원문 저장이 구현됐다고 해석하지 않는다.

권한 범위가 비거나 후보가 모두 걸러지면 `NO_CONTEXT / NONE`, 인용 빈 목록으로 끝나고 생성 포트를 호출하지 않는다. 이 분기는 `emptyScopeAvoidsRetrievalAndGeneration`, `filteredOrMissingEvidenceAvoidsGeneration`, `emptyRetrievalAvoidsGeneration`에서 확인한다. 이 내용은 기존 코드·테스트 설명이며 오늘 추가한 동작이 아니다.

## 테스트가 확인한 범위

| 테스트 소스 | 사용하는 값과 확인 내용 | 한계 |
|---|---|---|
| [AnswerControllerTest](../rag-adapters/src/test/java/io/github/ragportfolio/adapters/AnswerControllerTest.java) — 7건 | standalone MockMvc + 대역 usecase. Actor는 demo, 반환값은 `[MOCK] fixture`/빈 인용. DTO·고정 신원·기본값·오류/타입 계약 확인 | 실제 `QuestionService`와 fixture adapter를 HTTP 뒤에 함께 붙인 테스트가 아님 |
| [QuestionServiceTest](../rag-application/src/test/java/io/github/ragportfolio/application/QuestionServiceTest.java) — 12건 | 정상 case는 Actor `tenant/user`, 근거 `current` v1 / `fixture:1`, 답변 `[MOCK] answer`. 포트에 허용된 현재 근거만 전달되는지 확인 | demo 값과 다른 synthetic fixture를 쓰는 유스케이스 테스트이며 HTTP 전송 없음 |
| [DemoWiringTest](../rag-bootstrap/src/test/java/io/github/ragportfolio/bootstrap/DemoWiringTest.java) — 3건 | 실제 demo 조립에서 `useCase.ask(Actor(demo-tenant,demo-user),"fixture question",5)` 직접 호출. mode `MOCK`와 citation docID `demo-document`, 기본 프로필 분리·실제 Boot mapper 설정 확인 | HTTP 소켓 없음. 위 예상 JSON 전체의 문자열·버전·locator까지 하나의 assertion으로 검증한 것은 아님 |

따라서 소스 추적과 위 세 테스트를 대조한 문서다. 실제 HTTP end-to-end 테스트나 모델 호출·품질 평가를 수행한 기록으로 쓰지 않는다.


## 기록된 검사와 재현

2026-10-30에 application 12 + HTTP 7 + wiring 3 = **22건**의 선별 테스트가 통과했다. 이 검사는 test phase였고 실제 HTTP end-to-end 호출은 없었다. 22건은 기존 테스트의 일부이며 별도의 새 테스트 22개가 아니다. 이후 추가된 합성 eval 계약을 포함한 전체 31건 결과는 [검증 기록](verification.md)에 있다.

아래는 환경별 캐시 경로를 제외한 portable 재현 명령이다. 기록된 원시 로그와 동일한 환경을 보장하는 명령은 아니며 의존성이 준비된 캐시에서만 `-o`를 추가한다.

```powershell
.\mvnw.cmd -B -ntp -pl rag-bootstrap -am '-Dtest=QuestionServiceTest,AnswerControllerTest,DemoWiringTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

읽는 순서는 이 문서의 입력/예상 응답 → 호출 표의 소스 → 테스트 범위 표 → [합성 case 계약](eval-case-contract.md) → [개발 요약](weekly-review.md)과 [백로그](backlog.md)다. 세 테스트가 각각 확인하는 HTTP 대역/별도 synthetic fixture/demo 직접 조립의 경계를 유지한다.
