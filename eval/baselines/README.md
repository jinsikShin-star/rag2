# 비교군 계획 (모두 미실행)

| ID | 검색 전략 | 목적 |
|---|---|---|
| B0 | Elasticsearch lexical/BM25 | 설명 가능한 기준선 |
| B1 | Elasticsearch hybrid | 같은 corpus/권한/예산에서 dense 추가 효과 |
| B2 | PageIndex의 채택 구현 | 같은 조건에서 구조 기반 탐색 비교 |

현재 fixture retrieval과 mock generation은 계약 테스트 도구이며 B0/B1/B2 성과가 아니다. PageIndex 설정/모델 사용/비용은 공식 구현을 선정한 뒤 확정한다.

문서 snapshot·청크 정책·ACL·질문·topK·후처리·생성 모델·프롬프트·토큰 예산을 고정하고 검색 전략 하나씩 바꾼다. 정책 차이가 불가피하면 함께 보고한다. 후보 재랭킹은 별도 실험으로 분리한다.

권한 누출/삭제 근거 사용/과거 버전 인용은 품질 평균과 별도의 실패 gate다. 비교 결과는 `eval/results/<run-id>.json`에 저장한다. 현재 유의미한 점수나 승자를 주장할 자료는 없다.
