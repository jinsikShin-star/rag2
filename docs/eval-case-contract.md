# 합성 평가 case 로더의 최소 계약

확인일: 2026-11-02 월요일. 범위는 **기존 JSONL 원본을 읽어 평가 label의 기초 관계를 검사하는 테스트 하나**다. API·검색·모델의 운영 로더나 품질 평가기는 만들지 않는다.

## 1. 구체적인 시나리오

`synthetic-001`의 질문은 “문서 보존 기간은 며칠인가?”다. 호출자는 `demo-tenant / demo-user`이고 정답 근거는 `demo-tenant/guide`의 현재 v1, `section:retention`이다. 같은 `guide` ID라도 다른 테넌트 문서는 답에 섞이면 안 된다.

평가를 시작하기 전에 label이 가리키는 문서가 실제 파일에 있는지, ACTIVE/현재 버전인지, 호출자에게 읽기 권한이 있는지 확인해야 한다. 정답 label 자체가 잘못돼 있으면 나중에 검색 전략의 결과를 올바르게 비교하기 어렵다. 오늘은 이 입력 계약까지만 다룬다.

## 2. 실행 순서

1. Maven이 원본 `eval/datasets/synthetic-v1/`을 테스트 classpath의 `eval/synthetic-v1/`로 복사한다. 원본 데이터 파일을 새 위치에 중복 관리하지 않는다.
2. 테스트 안의 작은 reader가 classpath에서 `documents.jsonl`과 `cases.jsonl`을 UTF-8로 읽는다. 네트워크·API·검색엔진은 거치지 않는다.
3. 문서 5개와 case 6개가 로드되는지 확인하고 ID/label/인용이 참조하는 현재 문서와 호출자의 관계를 검사한다.
4. 계약이 맞으면 테스트가 통과한다. 결과 파일의 품질 점수를 채우거나 실제 답변을 생성하지 않는다.

## 3. 각 단계의 개념·역할·효과

| 개념 | 여기서 맡는 역할 | 확인 효과와 한계 |
|---|---|---|
| JSONL | 한 줄에 JSON 한 객체를 둔 기존 합성 파일 | 현재 6case/5문서를 반복해서 읽을 수 있음. 새 데이터 형식이나 수집 파이프라인은 아님 |
| classpath test resource | Maven 실행 위치에 덜 의존하도록 테스트 입력 연결 | 원본은 `eval/` 한 곳에 두고 복사본은 `target/test-classes` 빌드 출력으로만 사용 |
| outcome label | `ANSWER_WITH_EVIDENCE` 또는 `NO_CONTEXT` 기대값 | 필수 인용 유무와 label의 일관성을 검사. 실제 무응답/정답 판정의 품질 측정은 아님 |
| 문서 키·현재 버전·권한 | 인용이 허용된 현재 문서를 가리키는지 대조 | 테넌트/읽기 권한/ACTIVE/버전/locator와 금지 목록의 충돌을 확인. 운영 권한 저장소나 동시성 보장은 아님 |
| 테스트 전용 JSON 읽기 | 기존 BOM 관리 Jackson을 test scope에서 사용 | 운영 도메인·유스케이스에 JSON/Spring 의존성을 추가하지 않음 |

이 Java 계약은 [case JSON Schema](../eval/case.schema.json) 전체를 실행하는 schema engine이 아니다. 검색 관련도, 실제 생성 충실성, Recall/MRR, 토큰·비용·지연도 계산하지 않는다. API demo fixture와 평가용 합성 파일은 별도다.

## 4. 코드와 읽는 순서

1. [원본 case](../eval/datasets/synthetic-v1/cases.jsonl)에서 `synthetic-001`을 읽는다.
2. [원본 문서](../eval/datasets/synthetic-v1/documents.jsonl)의 동일 documentKey·version·locator·권한과 비교한다.
3. [테스트 모듈 POM](../rag-architecture-tests/pom.xml)의 test dependency/testResources 연결을 확인한다.
4. [SyntheticDatasetContractTest](../rag-architecture-tests/src/test/java/io/github/ragportfolio/evaluation/SyntheticDatasetContractTest.java)의 단일 `@Test`와 로컬 reader를 읽는다. 별도 운영 모듈이나 범용 loader API는 없다.
5. [검증 기록](verification.md)의 실제 RED/GREEN·전체 결과와 미실행 범위를 확인한다.

프로젝트 root에서 선별 실행하는 재현 명령이다. 환경별 로컬 캐시 경로 옵션을 제거한 portable 예시이며 과거 실행 로그 원문은 아니다:

```powershell
.\mvnw.cmd -B -ntp -pl rag-architecture-tests -am '-Dtest=SyntheticDatasetContractTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

전체 회귀는 `mvnw.cmd -B verify`로 실행한다. 의존성이 준비된 동일 캐시에서만 `-o`를 추가할 수 있다.


## 5. 기록된 결과와 제한

2026-11-02에 리소스 미연결 상태의 missing-resource assertion 실패를 확인한 후 testResources를 연결해 신규 계약 1건이 통과했다. 전체 오프라인 `verify`는 기존 30건과 새 계약 1건을 합쳐 **31건 통과, 실패·오류·스킵 0**이었다. 새 테스트 소스를 컴파일할 때 deprecated API 경고는 현재 API로 수정했으며 `-Xlint:all -Werror`를 유지했다.

메서드는 `loadsSyntheticCasesWithConsistentEvidenceLabels()` 하나이며 작은 private reader/필드 helper를 사용한다. ID·문서 키·6slice·질문/topK·outcome과 인용 유무·현재 문서/권한/버전/위치·금지 인용 충돌을 확인한다. 원본 JSONL과 classpath 복사본의 바이트가 일치한다.

Java 검사는 전체 JSON Schema 엔진이 아니다. 실제 검색/모델/서버·품질 점수·비용/부하·운영 데이터/보안 검증·온톨로지/KG/GNN은 구현하거나 실행하지 않았다. [검증 기록](verification.md)에서 정확한 검사 범위를 확인한다.
