import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { toast } from "sonner";
import { useAuth } from "../../context/AuthContext";

const passwordSchema = z
  .object({
    currentPassword: z
      .string()
      .min(6, "Password must be at least 6 characters"),
    newPassword: z
      .string()
      .min(6, "New Password must be at least 6 characters"),
    confirmPassword: z
      .string()
      .min(6, "Password confirmation must be at least 6 characters"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type PasswordFormValues = z.infer<typeof passwordSchema>;

const PasswordChangePage = () => {
  const { user, updatePassword } = useAuth();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting, isDirty, isValid },
  } = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  useEffect(() => {
    if (user) {
      reset({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    }
  }, [user]);

  const onSubmit = async (data: PasswordFormValues) => {
    try {
      await updatePassword(data);

      toast.success("Profile updated successfully");
    } catch (error : any) {
      toast.error(error);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 bg-[#161616] rounded-xl border border-zinc-800 text-white">
      <h1 className="text-2xl font-bold mb-6">Change Password</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        <div className="grid gap-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">Current Password</Label>
            <Input
              {...register("currentPassword")}
              type="password"
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.currentPassword && (
              <p className="text-red-500 text-sm">
                {errors.currentPassword.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="newPassword">
              New Password (Password must be at least 6 characters)
            </Label>
            <Input
              {...register("newPassword")}
              type="password"
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.newPassword && (
              <p className="text-red-500 text-sm">
                {errors.newPassword.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Confirm Password</Label>
            <Input
              {...register("confirmPassword")}
              type="password"
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.confirmPassword && (
              <p className="text-red-500 text-sm">
                {errors.confirmPassword.message}
              </p>
            )}
          </div>
        </div>

        <div className="flex justify-end gap-4">
          <Button
            type="submit"
            disabled={isSubmitting || !isDirty || !isValid}
            className="bg-emerald-500 hover:bg-emerald-400 text-black font-semibold hover:cursor-pointer"
          >
            {isSubmitting ? "Saving..." : "Save Changes"}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default PasswordChangePage;
