import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { AuthProvider, useAuth } from "../../context/AuthContext";
import PublicRoute from "../../components/routes/PublicRoute";
import LoginPage from "./LoginPage";
import VerifyEmailPage from "./VerifyEmailPage";
import api from "../../services/api";

vi.mock("../../services/api", () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const mockedApi = vi.mocked(api, true);

const oldAccount = {
  firstName: "Old",
  lastName: "Account",
  email: "old@example.com",
  role: "ROLE_USER",
  digestEnabled: true,
  emailVerified: true,
};

const WhoIsLoggedIn = () => {
  const { user, isAuthenticated } = useAuth();
  return <p>{isAuthenticated ? `Home: signed in as ${user?.email}` : "Home: guest"}</p>;
};

const renderAt = (url: string) =>
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[url]}>
        <Routes>
          <Route path="/" element={<WhoIsLoggedIn />} />
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<LoginPage />} />
          </Route>
          <Route path="/verify-email" element={<VerifyEmailPage />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  );

describe("VerifyEmailPage", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("signs out the old account and lands on the sign-in page after verifying", async () => {
    localStorage.setItem("token", "old-account-jwt");
    mockedApi.get.mockResolvedValue({ data: oldAccount });
    mockedApi.post.mockResolvedValue({ data: { message: "Email verified." } });

    renderAt("/verify-email?token=abc123");

    expect(await screen.findByText("Email verified. Please sign in.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
    expect(screen.queryByText(/signed in as old@example.com/)).not.toBeInTheDocument();
    expect(localStorage.getItem("token")).toBeNull();
    expect(mockedApi.post).toHaveBeenCalledWith("/auth/verify-email", { token: "abc123" });
    expect(mockedApi.post).toHaveBeenCalledTimes(1);
  });

  it("lands on the sign-in page when nobody is logged in", async () => {
    mockedApi.post.mockResolvedValue({ data: { message: "Email verified." } });

    renderAt("/verify-email?token=abc123");

    expect(await screen.findByText("Email verified. Please sign in.")).toBeInTheDocument();
    expect(mockedApi.get).not.toHaveBeenCalled();
  });

  it("shows the error and keeps the current session when the link is invalid", async () => {
    localStorage.setItem("token", "old-account-jwt");
    mockedApi.get.mockResolvedValue({ data: oldAccount });
    mockedApi.post.mockRejectedValue({
      response: {
        data: {
          detail: "This verification link is invalid or has already been used.",
          code: "INVALID_TOKEN",
        },
      },
    });

    renderAt("/verify-email?token=used-token");

    expect(
      await screen.findByText("This verification link is invalid or has already been used."),
    ).toBeInTheDocument();
    expect(screen.getByText("Verification failed")).toBeInTheDocument();
    expect(localStorage.getItem("token")).toBe("old-account-jwt");
  });

  it("shows an error without calling the API when the link has no token", async () => {
    renderAt("/verify-email");

    expect(await screen.findByText("This verification link is invalid.")).toBeInTheDocument();
    expect(mockedApi.post).not.toHaveBeenCalled();
  });
});
