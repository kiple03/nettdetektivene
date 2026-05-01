import {getToken, setToken, clearToken, markSessionEnded, isSessionEnded, decodeToken, isLoggedIn, getRole,
} from "@/stores/authStore";

describe("authStore", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("setter og henter token", () => {
    setToken("abc123");
    expect(getToken()).toBe("abc123");
  });

  it("clearer token", () => {
    setToken("abc123");
    clearToken();
    expect(getToken()).toBeNull();
  });

  it("markerer session som avsluttet", () => {
    markSessionEnded();
    expect(isSessionEnded()).toBe(true);
  });

  it("decodeToken returnerer null for ugyldig token", () => {
    expect(decodeToken("invalid")).toBeNull();
  });

  it("dekoder gyldig token", () => {
    const payload = {
      sub: "Ingve",
      role: "STUDENT",
      exp: Math.floor(Date.now() / 1000) + 1000,
    };

    const base64 = btoa(JSON.stringify(payload));
    const token = `xxx.${base64}.yyy`;

    const result = decodeToken(token);

    expect(result?.username).toBe("Ingve");
    expect(result?.role).toBe("STUDENT");
  });

  it("isLoggedIn er false uten token", () => {
    expect(isLoggedIn()).toBe(false);
  });

  it("isLoggedIn er true for gyldig token", () => {
    const payload = {
      sub: "Ingve",
      role: "STUDENT",
      exp: Math.floor(Date.now() / 1000) + 1000,
    };

    const base64 = btoa(JSON.stringify(payload));
    const token = `xxx.${base64}.yyy`;

    setToken(token);

    expect(isLoggedIn()).toBe(true);
  });

  it("isLoggedIn er false for expired token", () => {
    const payload = {
      sub: "Ingve",
      role: "STUDENT",
      exp: Math.floor(Date.now() / 1000) - 1000,
    };

    const base64 = btoa(JSON.stringify(payload));
    const token = `xxx.${base64}.yyy`;

    setToken(token);

    expect(isLoggedIn()).toBe(false);
  });

  it("getRole returnerer riktig rolle", () => {
    const payload = {
      sub: "Ingve",
      role: "TEACHER",
      exp: Math.floor(Date.now() / 1000) + 1000,
    };

    const base64 = btoa(JSON.stringify(payload));
    const token = `xxx.${base64}.yyy`;

    setToken(token);

    expect(getRole()).toBe("TEACHER");
  });

  it("fjerner ROLE_ prefix", () => {
    const payload = {
      sub: "Ingve",
      role: "ROLE_STUDENT",
    };

    const base64 = btoa(JSON.stringify(payload));
    const token = `xxx.${base64}.yyy`;

    const result = decodeToken(token);

    expect(result?.role).toBe("STUDENT");
  });
});