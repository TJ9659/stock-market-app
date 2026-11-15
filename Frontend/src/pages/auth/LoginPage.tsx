import { useForm } from "react-hook-form";
import { useNavigate, Link, useSearchParams } from "react-router";
import { useAuth, type AuthError } from "../../context/AuthContext";
import { Mail, Lock, ArrowRight, CircleAlert, CircleCheck } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

const LoginPage = () => {
  const { login, resendVerification } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const justVerified = searchParams.get("verified") === "1";
  const [serverError, setServerError] = useState<string | null>(null);
  const [unverifiedEmail, setUnverifiedEmail] = useState<string | null>(null);
  const [resending, setResending] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: { email: string; password: string }) => {
    setServerError(null);
    setUnverifiedEmail(null);
    try {
      await login(data);
      navigate("/markets");
    } catch (error) {
      const authError = error as AuthError;
      setServerError(authError.message);
      if (authError.code === "EMAIL_NOT_VERIFIED") {
        setUnverifiedEmail(data.email);
      }
    }
  };

  const resend = async () => {
    if (!unverifiedEmail) return;
    setResending(true);
    try {
      await resendVerification(unverifiedEmail);
      toast.success(`Verification email sent to ${unverifiedEmail}`);
    } catch {
      toast.error("Could not resend the email. Please try again.");
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-md p-8 rounded-3xl border border-gray-800 bg-[#161616] shadow-2xl">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 rounded-2xl bg-emerald-500/10 mb-4">
            <img src="/images/logo-heading.svg" alt="Logo" className="w-20 h-20" />
          </div>
          <h1 className="text-3xl font-black text-white tracking-tighter">
            Welcome Back
          </h1>
          <p className="text-gray-500 text-sm mt-2">
            Log in to manage your watchlist and AI insights
          </p>
        </div>

        {justVerified && !serverError && (
          <div className="flex items-center justify-center gap-2 mb-6 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-bold">
            <CircleCheck size={18} /> <span>Email verified. Please sign in.</span>
          </div>
        )}

        {serverError && (
          <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs font-bold">
            <div className="flex items-center justify-center gap-2">
              <CircleAlert size={18}/> <span>{serverError}</span>
            </div>
            {unverifiedEmail && (
              <button
                type="button"
                onClick={resend}
                disabled={resending}
                className="mt-3 w-full text-emerald-400 hover:text-emerald-300 underline disabled:opacity-50 hover:cursor-pointer"
              >
                {resending ? "Sending..." : "Resend verification email"}
              </button>
            )}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
              Email Address
            </label>
            <div className="relative">
              <Mail
                className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500"
                size={18}
              />
              <input
                {...register("email", {
                  required: "Email is required",
                  pattern: {
                    value: /^\S+@\S+$/i,
                    message: "Invalid email format",
                  },
                })}
                className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3.5 pl-12 pr-4 text-white placeholder:text-gray-600 focus:outline-none focus:border-emerald-500 transition-all"
                placeholder="name@company.com"
              />
            </div>
            {errors.email && (
              <p className="text-red-500 text-[10px] mt-1.5 ml-1 font-bold">
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
              Password
            </label>
            <div className="relative">
              <Lock
                className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500"
                size={18}
              />
              <input
                type="password"
                {...register("password", { required: "Password is required" })}
                className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3.5 pl-12 pr-4 text-white placeholder:text-gray-600 focus:outline-none focus:border-emerald-500 transition-all"
                placeholder="••••••••"
              />
            </div>
            {errors.password && (
              <p className="text-red-500 text-[10px] mt-1.5 ml-1 font-bold">
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-emerald-500 text-black font-black py-4 rounded-xl flex items-center justify-center gap-2 hover:bg-emerald-400 active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed mt-4 uppercase tracking-widest text-sm"
          >
            {isSubmitting ? (
              "Authenticating..."
            ) : (
              <>
                Sign In <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-sm">
          <span className="text-gray-500">Don't have an account? </span>
          <Link
            to="/register"
            className="text-emerald-500 font-bold hover:text-emerald-400 transition-colors"
          >
            Create Account
          </Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
