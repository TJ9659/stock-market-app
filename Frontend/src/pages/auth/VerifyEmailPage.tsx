import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { CircleAlert, LoaderCircle } from "lucide-react";
import { useAuth, type AuthError } from "../../context/AuthContext";

const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const { verifyEmail, logout } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(
    token ? null : "This verification link is invalid.",
  );
  const started = useRef(false);

  useEffect(() => {
    if (!token || started.current) return;
    started.current = true;

    verifyEmail(token)
      .then(() => {
        logout();
        navigate("/login?verified=1", { replace: true });
      })
      .catch((e: AuthError) => setError(e.message));
  }, [token, verifyEmail, logout, navigate]);

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-md p-8 rounded-3xl border border-gray-800 bg-[#161616] shadow-2xl text-center">
        {error ? (
          <>
            <div className="inline-flex p-3 rounded-2xl bg-red-500/10 mb-4 text-red-500">
              <CircleAlert size={32} />
            </div>
            <h1 className="text-2xl font-black text-white tracking-tighter">
              Verification failed
            </h1>
            <p className="text-gray-400 text-sm mt-3">{error}</p>
            <p className="text-gray-500 text-xs mt-2">
              Try signing in to get a new verification email.
            </p>
            <Link
              to="/login"
              className="inline-block mt-6 bg-emerald-500 text-black font-black py-3 px-6 rounded-xl hover:bg-emerald-400 transition-all uppercase tracking-widest text-sm"
            >
              Back to sign in
            </Link>
          </>
        ) : (
          <>
            <LoaderCircle className="mx-auto animate-spin text-emerald-500" size={32} />
            <p className="text-gray-400 text-sm mt-4">Verifying your email...</p>
          </>
        )}
      </div>
    </div>
  );
};

export default VerifyEmailPage;
