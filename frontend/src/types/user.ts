export interface UserProfileResponse {
  email: string
  name: string
  ttorae: string
  affiliationName: string | null
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  newPasswordConfirm: string
}

export interface VerifyIdentityRequest {
  name: string
  ttorae: string
  affiliationId: number | null
  email: string
}

export interface ResetPasswordRequest extends VerifyIdentityRequest {
  newPassword: string
  newPasswordConfirm: string
}
