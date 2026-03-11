export interface UserProfileResponse {
  email: string
  name: string
  ttorae: number
  affiliationName: string | null
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  newPasswordConfirm: string
}

export interface VerifyIdentityRequest {
  name: string
  ttorae: number
  affiliationId: number
  email: string
}

export interface ResetPasswordRequest extends VerifyIdentityRequest {
  newPassword: string
  newPasswordConfirm: string
}
