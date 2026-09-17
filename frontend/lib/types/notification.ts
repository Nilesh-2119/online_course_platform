export interface AppNotification {
  id: string | number
  title: string
  message: string
  tag?: string
  linkUrl?: string
  active: boolean
  isNew: boolean
  createdAt: string
  updatedAt?: string
}

export interface CreateNotificationDto {
  title: string
  message: string
  tag?: string
  linkUrl?: string
  active?: boolean
  isNew?: boolean
}

export interface UpdateNotificationDto {
  title?: string
  message?: string
  tag?: string
  linkUrl?: string
  active?: boolean
  isNew?: boolean
}
