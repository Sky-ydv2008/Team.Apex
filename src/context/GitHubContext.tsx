"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { GitHubConnectionState } from "@/lib/validation/publish";
import { isSupabaseConfigured, createSupabaseBrowserClient } from "@/lib/supabase/client";
import { useAuth } from "./AuthContext";

interface GitHubContextType {
  githubState: GitHubConnectionState;
  loading: boolean;
  connectGithub: () => Promise<void>;
  connectDemoGithub: () => void;
  disconnectGithub: () => Promise<void>;
}

const GITHUB_STORAGE_KEY = "projectforge_github_connection";

const DEFAULT_DEMO_GITHUB: GitHubConnectionState = {
  isConnected: true,
  username: "Sky-ydv2008",
  avatarUrl: "https://avatars.githubusercontent.com/u/Sky-ydv2008?v=4",
  scopes: ["repo", "user", "workflow"],
  connectedAt: new Date().toISOString(),
};

const GitHubContext = createContext<GitHubContextType | undefined>(undefined);

export function GitHubProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const [githubState, setGithubState] = useState<GitHubConnectionState>({
    isConnected: false,
    scopes: ["repo", "user"],
  });
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    async function loadGitHubStatus() {
      setLoading(true);

      // Check localStorage
      const cached = localStorage.getItem(GITHUB_STORAGE_KEY);
      if (cached) {
        try {
          setGithubState(JSON.parse(cached));
          setLoading(false);
          return;
        } catch {
          // fallback
        }
      }

      // Check Supabase if logged in
      if (isSupabaseConfigured && user && !user.isDemoUser) {
        const supabase = createSupabaseBrowserClient();
        if (supabase) {
          const { data, error } = await supabase
            .from("github_connections")
            .select("*")
            .eq("user_id", user.id)
            .single();

          if (data && !error) {
            const state: GitHubConnectionState = {
              isConnected: true,
              username: data.provider_user_id || "Sky-ydv2008",
              avatarUrl: `https://avatars.githubusercontent.com/u/${data.provider_user_id || 'Sky-ydv2008'}`,
              scopes: data.scopes || ["repo", "user"],
              connectedAt: data.created_at,
            };
            setGithubState(state);
            localStorage.setItem(GITHUB_STORAGE_KEY, JSON.stringify(state));
            setLoading(false);
            return;
          }
        }
      }

      // Auto-connect real account in Demo Mode
      setGithubState(DEFAULT_DEMO_GITHUB);
      localStorage.setItem(GITHUB_STORAGE_KEY, JSON.stringify(DEFAULT_DEMO_GITHUB));
      setLoading(false);
    }

    loadGitHubStatus();
  }, [user]);

  const connectDemoGithub = () => {
    setGithubState(DEFAULT_DEMO_GITHUB);
    localStorage.setItem(GITHUB_STORAGE_KEY, JSON.stringify(DEFAULT_DEMO_GITHUB));
  };

  const connectGithub = async () => {
    const clientId = process.env.NEXT_PUBLIC_GITHUB_CLIENT_ID;
    if (!clientId) {
      connectDemoGithub();
      return;
    }

    const redirectUri = `${window.location.origin}/api/integrations/github/callback`;
    const scope = "repo user workflow";
    window.location.href = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent(scope)}`;
  };

  const disconnectGithub = async () => {
    setGithubState({ isConnected: false, scopes: ["repo", "user"] });
    localStorage.removeItem(GITHUB_STORAGE_KEY);

    if (isSupabaseConfigured && user && !user.isDemoUser) {
      const supabase = createSupabaseBrowserClient();
      if (supabase) {
        await supabase.from("github_connections").delete().eq("user_id", user.id);
      }
    }
  };

  return (
    <GitHubContext.Provider
      value={{
        githubState,
        loading,
        connectGithub,
        connectDemoGithub,
        disconnectGithub,
      }}
    >
      {children}
    </GitHubContext.Provider>
  );
}

export function useGitHub() {
  const context = useContext(GitHubContext);
  if (!context) {
    throw new Error("useGitHub must be used within a GitHubProvider");
  }
  return context;
}
