import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router";
import { MailCheck } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "../../context/AuthContext";

const RESEND_COOLDOWN_SECONDS = 60;

const CheckEmailPage = () => {
  const [searchParams] = useSearchParams();
  const email = searchParams.get("email") ?? "";
  const { resendVerification } = useAuth();
  const [cooldown, setCooldown] = useState(RESEND_COOLDOWN_SECONDS);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setTimeout(() => setCooldown((s) => s - 1), 1000);
    return () => clearTimeout(timer);
  }, [cooldown]);

  const resend = async () => {
    try {
      await resendVerification(email);
      toast.success(`We sent a new link to ${email}`);
      setCooldown(RESEND_COOLDOWN_SECONDS);
    } catch {
      toast.error("Could not resend the email. Please try again.");
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-md p-8 rounded-3xl border border-gray-800 bg-[#161616] shadow-2xl text-center">
        <div className="inline-flex p-3 rounded-2xl bg-emerald-500/10 mb-4 text-emerald-500">
          <MailCheck size={32} />
        </div>
        <h1 className="text-3xl font-black text-white tracking-tighter">
          Check your email
        </h1>
        <p className="text-gray-400 text-sm mt-3">
          We sent a verification link to{" "}
          <span className="text-white font-bold">{email || "your email"}</span>.
          Click it to activate your account, then sign in.
        </p>

        <button
          type="button"
          onClick={resend}
          disabled={!email || cooldown > 0}
          className="w-full mt-8 bg-[#1f1f1f] border border-gray-800 text-white font-bold py-3.5 rounded-xl hover:border-emerald-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:cursor-pointer text-sm"
        >
          {cooldown > 0 ? `Resend email in ${cooldown}s` : "Resend email"}
        </button>

        <div className="mt-6 text-sm">
          <span className="text-gray-500">Already verified? </span>
          <Link
            to="/login"
            className="text-emerald-500 font-bold hover:text-emerald-400 transition-colors"
          >
            Sign in
          </Link>
        </div>
      </div>
    </div>
  );
};

export default CheckEmailPage;
