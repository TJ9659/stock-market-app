import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import * as z from "zod";
import { Mail } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { toast } from "sonner";
import { useAuth } from "../../context/AuthContext";
import api from "../../services/api";

const profileSchema = z.object({
  firstName: z.string().min(3, "First name must be at least 3 characters"),
  lastName: z.string().min(3, "Last name must be at least 3 characters"),
  email: z.string().email("Invalid email address"),
  digestEnabled: z.boolean(),
});

type ProfileFormValues = z.infer<typeof profileSchema>;

const ProfileEditPage = () => {
  const { user, updateUser } = useAuth();
  const [sendingTest, setSendingTest] = useState(false);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting, isDirty, isValid },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName,
      lastName: user?.lastName,
      email: user?.email,
      digestEnabled: user?.digestEnabled ?? true,
    },
  });

  useEffect(() => {
    if (user) {
      reset({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        digestEnabled: user.digestEnabled ?? true,
      });
    }
  }, [user, reset]);

  const onSubmit = async (data: ProfileFormValues) => {
    try {
      await updateUser(data);

      toast.success("Profile updated successfully");
    } catch {
      toast.error("Failed to update profile");
    }
  };

  const sendTestDigest = async () => {
    setSendingTest(true);
    try {
      const response = await api.post("/digest/test");
      toast.success(
        `Test digest with ${response.data.stocks} stock(s) sent to ${response.data.email}`,
      );
    } catch (error) {
      const detail = isAxiosError(error) ? error.response?.data?.detail : null;
      toast.error(detail ?? "Could not send the test digest");
    } finally {
      setSendingTest(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 bg-[#161616] rounded-xl border border-zinc-800 text-white">
      <h1 className="text-2xl font-bold mb-6">Edit Profile</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        <div className="grid gap-4">
          <div className="space-y-2">
            <Label htmlFor="firstName">First Name</Label>
            <Input
              {...register("firstName")}
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.firstName && (
              <p className="text-red-500 text-sm">{errors.firstName.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="lastName">Last Name</Label>
            <Input
              {...register("lastName")}
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.lastName && (
              <p className="text-red-500 text-sm">{errors.lastName.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email Address</Label>
            <Input
              {...register("email")}
              type="email"
              className="bg-zinc-900 border-zinc-800 focus:ring-emerald-500"
            />
            {errors.email && (
              <p className="text-red-500 text-sm">{errors.email.message}</p>
            )}
          </div>
        </div>

        <div className="rounded-lg border border-zinc-800 bg-zinc-900/60 p-4 space-y-4">
          <label
            htmlFor="digestEnabled"
            className="flex items-start gap-3 hover:cursor-pointer"
          >
            <input
              id="digestEnabled"
              type="checkbox"
              {...register("digestEnabled")}
              className="mt-1 h-4 w-4 accent-emerald-500 hover:cursor-pointer"
            />
            <span>
              <span className="block font-semibold">Daily email digest</span>
              <span className="block text-sm text-zinc-400">
                Weekday mornings at 8:00 AM (SGT): your watchlist's closing
                prices, AI sentiment and top headlines.
              </span>
            </span>
          </label>

          <Button
            type="button"
            onClick={sendTestDigest}
            disabled={sendingTest}
            className="bg-zinc-800 hover:bg-zinc-700 text-white hover:cursor-pointer"
          >
            <Mail size={16} />
            {sendingTest ? "Sending..." : "Send me a test digest"}
          </Button>
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

export default ProfileEditPage;
