export type Role = 'ADMIN' | 'USER' | 'PASTOR' | 'MOKJANG'

export type UserStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE'

export interface SignupRequest {
  name: string
  ttorae: string
  phone: string
  email: string
  password: string
  affiliationId: number | null
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RefreshRequest {
  refreshToken: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
}

export interface UserListResponse {
  id: number
  name: string
  ttorae: string
  phone: string
  email: string
  role: Role
  status: UserStatus
  createdAt: string
  affiliationId: number | null
  affiliationName: string | null
}

export interface ErrorResponse {
  errorCode: string
  message: string
}

export interface AuthUser {
  id: number
  email: string
  role: Role
}

export interface JwtPayload {
  sub: string
  email: string
  role: Role
  iat: number
  exp: number
}
