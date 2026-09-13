import { z } from "zod"

/** Login form validation schema. */
export const loginSchema = z.object({
  email: z.string().email("Please enter a valid email address."),
  password: z.string().min(6, "Password must be at least 6 characters."),
})

export type LoginInput = z.infer<typeof loginSchema>

/** Registration form validation schema. */
export const registerSchema = z
  .object({
    name: z.string().min(2, "Name must be at least 2 characters."),
    email: z.string().email("Please enter a valid email address."),
    password: z.string().min(8, "Password must be at least 8 characters."),
    confirm: z.string(),
    acceptedTerms: z.literal(true, {
      errorMap: () => ({ message: "You must accept the Terms & Privacy Policy." }),
    }),
    marketingConsent: z.boolean().default(false),
  })
  .refine((data) => data.password === data.confirm, {
    message: "Passwords do not match.",
    path: ["confirm"],
  })

export type RegisterInput = z.infer<typeof registerSchema>

/** Checkout form validation schema. */
export const checkoutSchema = z.object({
  name: z.string().min(2, "Name is required."),
  email: z.string().email("Please enter a valid email address."),
  phone: z.string().optional(),
  couponCode: z.string().optional(),
  acceptedTerms: z.literal(true, {
    errorMap: () => ({ message: "You must accept the Terms & Conditions." }),
  }),
})

export type CheckoutInput = z.infer<typeof checkoutSchema>

/** Onboarding survey validation schema. */
export const onboardingSchema = z.object({
  role: z.string().min(1, "Please select what best describes you."),
  goals: z.array(z.string()).min(1, "Please select at least one goal."),
  experience: z.string().min(1, "Please select your experience level."),
})

export type OnboardingInput = z.infer<typeof onboardingSchema>

/** Admin coupon creation validation schema. */
export const couponSchema = z.object({
  code: z.string().min(3, "Coupon code must be at least 3 characters.").toUpperCase(),
  discountType: z.enum(["percentage", "fixed"]),
  discountValue: z.number().positive("Discount must be greater than 0."),
  usageLimit: z.number().int().positive("Usage limit must be a positive integer."),
  expiryDate: z.string().datetime("Valid expiry date required."),
})

export type CouponInput = z.infer<typeof couponSchema>

/** Admin free resource creation schema. */
export const resourceSchema = z.object({
  title: z.string().min(3, "Title must be at least 3 characters."),
  description: z.string().min(10, "Description must be at least 10 characters."),
  type: z.enum(["pdf", "doc", "spreadsheet", "archive", "other"]),
  status: z.enum(["draft", "published"]),
})

export type ResourceInput = z.infer<typeof resourceSchema>
