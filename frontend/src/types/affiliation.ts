export type MainAffiliation = 'SARANGBANG' | 'SAEGAJOK' | 'IMWONDAN' | 'MOKJANG' | 'SINON'

export interface AffiliationResponse {
  id: number
  mainAffiliation: MainAffiliation
  subAffiliation: string | null
  displayName: string
}

export const MAIN_AFFILIATION_LABELS: Record<MainAffiliation, string> = {
  SARANGBANG: '사랑방',
  SAEGAJOK: '새가족부',
  IMWONDAN: '임원단',
  MOKJANG: '목장',
  SINON: '신혼부부',
}
