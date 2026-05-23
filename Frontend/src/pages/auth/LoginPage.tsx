import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router";
import { useAuth } from "../../context/AuthContext";
import { Mail, Lock, ArrowRight, BarChart3, CircleAlert } from "lucide-react";
import api from "../../services/api";
import { useState } from "react";

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

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

  const onSubmit = async (data: any) => {
    setServerError(null);
    try {
      await login(data);
      navigate("/markets");
    } catch (error: any) {
      setServerError(
        error.response?.data?.message ||
          "Invalid credentials. Please try again.",
      );
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-md p-8 rounded-3xl border border-gray-800 bg-[#161616] shadow-2xl">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 rounded-2xl bg-emerald-500/10 mb-4">
            <BarChart3 className="text-emerald-500" size={32} />
          </div>
          <h1 className="text-3xl font-black text-white tracking-tighter">
            Welcome Back
          </h1>
          <p className="text-gray-500 text-sm mt-2">
            Log in to manage your watchlist and AI insights
          </p>
        </div>

        {serverError && (
          <div className="flex items-center justify-center gap-2 mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs font-bold">
           <CircleAlert size={18}/> <span>{serverError}</span>
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
