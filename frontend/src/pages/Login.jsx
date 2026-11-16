import './Login.css';

function Login() {
  return (
    <main className="login-container">
      <section className="login-card">
        <div className="login-brand">
          <span className="brand-icon">M</span>
          <h2>METALAB</h2>
          <p>Participe do sorteio</p>
        </div>

        <div className="login-heading">
          <h2>Bem-vindo</h2>
          <p>Entre com suas credenciais para continuar.</p>
        </div>

        <form className="login-form">
          <div className="form-group">
            <label htmlFor="email">E-mail</label>
            <input
              type="email"
              id="email"
              placeholder="seu@email.com"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Senha</label>
            <input
              type="password"
              id="password"
              placeholder="Digite sua senha"
              required
            />
          </div>

          <button type="submit">
            Entrar no sistema
          </button>
        </form>

        <p className="login-footer">
          METALAB © 2026 — Todos os direitos reservados
        </p>
      </section>
    </main>
  );
}

export default Login;