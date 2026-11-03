# 향후 평가 데이터와 baseline 비교

현재는 **합성 fixture·평가 규약·테스트 전용 로더 계약1건**을 제공한다. 품질 평가 실행기·실제 인덱스·모델·점수는 없다. 테스트의 mock 응답을 RAG 품질 성과로 해석하지 않는다. 실제 사용자·회사 문서를 넣지 않았다.

## 파일

- `datasets/synthetic-v1/documents.jsonl`: synthetic snapshot의 현재 메타데이터/짧은 내용
- `datasets/synthetic-v1/cases.jsonl`: 질의·호출자·기대 outcome·필수/금지 인용
- `case.schema.json`: case JSON Schema (draft 2020-12), 도구 선택 후 자동 검증에 연결할 위치
- `baselines/README.md`: 비교군·통제 변수
- `results/example-not-measured.json`: 결과 형식 예시. 모든 측정값 null

API demo fixture와 별도인 평가 설계 데이터다. 2026-11-02부터 기존 `rag-architecture-tests`의 `SyntheticDatasetContractTest`가 원본 JSONL을 testResources로 받아 실제 6case/5문서의 label/인용 관계를 읽고 검사한다. 이 연결은 테스트 전용이며 API가 데이터를 읽거나 품질 평가를 실행하는 기능은 아니다. [구체적 시나리오→실행 순서→개념/효과→코드와 검증 결과](../docs/eval-case-contract.md)를 참고한다.

## 먼저 구현할 평가 흐름

1. 같은 corpus/ACL snapshot, 문서 버전, query set으로 retrieval adapter만 교체한다.
2. 각 case에 대해 검색 후보와 필터 후 근거 ID/버전/locator를 저장한다. 권한 밖/삭제/과거 버전 근거는 생성 입력과 최종 인용 모두 0개여야 한다.
3. 허용 relevant document에 대해 Recall@k·MRR/nDCG를 정의한다. 이번 fixture는 최소 문서 수준 label이며 chunk 정답과 순위 grade는 아직 없어 nDCG를 바로 계산하면 안 된다.
4. 같은 생성 모델·프롬프트·토큰 예산에서 근거 충실성, 인용 정확도, 무응답 판정과 지연/토큰/비용을 비교한다.
5. 자유형 답변은 고정 문자열 일치만으로 채점하지 않는다. 사람 rubric을 먼저 정하고 LLM judge를 추가할 때는 모델/프롬프트를 고정하고 편향을 표본 검토한다. 유료 judge 호출은 별도 범위다.

## 결과와 재현성

run ID, 실행 시각, 코드 revision, 데이터 SHA-256, index build ID, ACL revision, retrieval 설정, provider/model 버전, prompt hash, seed(지원 시), topK/토큰 예산, warm/cold 상태를 기록한다. 비밀과 문서/질의 원문을 공개 결과에 넣지 않는다.

집계는 정상/무근거/타테넌트/권한거부/삭제/버전변경 slice를 나눠 보고한다. 평균 품질이 보안 실패를 숨기면 안 된다. 권한 누출은 별도 실패 gate다. 여러 번 실행해 지연 분포와 비용을 보고하고 미측정은 null로 보존한다.

실제 데이터는 권한과 공개 가능 범위를 확인한 뒤 추가한다. 문서 계열/템플릿별 train/dev/test 분리를 하고 test로 프롬프트/전략을 튜닝하지 않는다. 합성 6건은 파이프라인 검증용이지 품질 통계의 근거가 아니다.
