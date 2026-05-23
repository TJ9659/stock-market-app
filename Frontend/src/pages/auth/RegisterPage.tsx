import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router";
import { Mail, Lock, User, ArrowRight, ShieldCheck } from "lucide-react";
import { useState } from "react";
import api from "../../services/api";
import { useAuth } from "../../context/AuthContext";

const RegisterPage = () => {
  const navigate = useNavigate();
  const { registerUser } = useAuth();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const password = watch("password");

  const onSubmit = async (data: any) => {
    setServerError(null);
    try {
      const { confirmPassword, ...registerData } = data;
      await registerUser(registerData);

      navigate("/");
    } catch (error: any) {
      setServerError(
        error ||
          "Registration failed. Try a different email.",
      );
    }
  };

  return (
    <div className="min-h-[90vh] flex items-center justify-center py-12">
      <div className="w-full max-w-md p-8 rounded-3xl border border-gray-800 bg-[#161616] shadow-2xl">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 rounded-2xl bg-emerald-500/10 mb-4">
            <ShieldCheck className="text-emerald-500" size={32} />
          </div>
          <h1 className="text-3xl font-black text-white tracking-tighter">
            Join MarketSense
          </h1>
          <p className="text-gray-500 text-sm mt-2">
            Start tracking your portfolio with AI insights
          </p>
        </div>

        {serverError && (
          <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs font-bold text-center">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
                First Name
              </label>
              <div className="relative">
                <User
                  className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500"
                  size={16}
                />
                <input
                  {...register("firstName", { required: "Required" })}
                  className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3 pl-11 pr-4 text-sm text-white focus:border-emerald-500 outline-none transition-all"
                  placeholder="John"
                />
              </div>
              {errors.firstName && (
                <p className="text-red-500 text-[10px] mt-1 ml-1 font-bold">
                  {errors.firstName.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
                Last Name
              </label>
              <div className="relative">
                <input
                  {...register("lastName", { required: "Required" })}
                  className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3 px-4 text-sm text-white focus:border-emerald-500 outline-none transition-all"
                  placeholder="Doe"
                />
              </div>
              {errors.lastName && (
                <p className="text-red-500 text-[10px] mt-1 ml-1 font-bold">
                  {errors.lastName.message}
                </p>
              )}
            </div>
          </div>

          <div>
            <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
              Email
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
                className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3 pl-12 pr-4 text-white focus:border-emerald-500 outline-none transition-all"
                placeholder="name@company.com"
              />
            </div>
            {errors.email && (
              <p className="text-red-500 text-[10px] mt-1 ml-1 font-bold">
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
                {...register("password", {
                  required: "Password is required",
                  minLength: { value: 8, message: "Minimum 8 characters" },
                })}
                className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3 pl-12 pr-4 text-white focus:border-emerald-500 outline-none transition-all"
                placeholder="••••••••"
              />
            </div>
            {errors.password && (
              <p className="text-red-500 text-[10px] mt-1 ml-1 font-bold">
                {errors.password.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-[10px] uppercase font-black text-gray-500 mb-1.5 ml-1 tracking-widest">
              Confirm Password
            </label>
            <div className="relative">
              <Lock
                className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500"
                size={18}
              />
              <input
                type="password"
                {...register("confirmPassword", {
                  required: "Please confirm your password",
                  validate: (value) =>
                    value === password || "Passwords do not match",
                })}
                className="w-full bg-[#1f1f1f] border border-gray-800 rounded-xl py-3 pl-12 pr-4 text-white outline-none focus:border-emerald-500 transition-all"
                placeholder="••••••••"
              />
            </div>
            {errors.confirmPassword && (
              <p className="text-red-500 text-[10px] mt-1 ml-1 font-bold">
                {errors.confirmPassword.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-emerald-500 text-black font-black py-4 rounded-xl flex items-center justify-center gap-2 hover:bg-emerald-400 transition-all disabled:opacity-50 mt-6 uppercase tracking-widest text-sm"
          >
            {isSubmitting ? (
              "Creating Account..."
            ) : (
              <>
                Create Account <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-sm text-gray-500">
          Already a member?{" "}
          <Link to="/login" className="text-emerald-500 font-bold">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
