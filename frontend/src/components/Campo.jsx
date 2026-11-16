export default function Campo({ id, rotulo, dica, erro, ...props }) {
  const idErro = `erro-${id}`;
  return (
    <div className="campo">
      <label htmlFor={`campo-${id}`}>
        {rotulo}
        {dica && <span className="dica">{dica}</span>}
      </label>
      <input
        id={`campo-${id}`}
        name={id}
        aria-invalid={erro ? 'true' : 'false'}
        aria-describedby={erro ? idErro : undefined}
        {...props}
      />
      {erro && (
        <p className="erro" id={idErro} role="alert">
          {erro}
        </p>
      )}
    </div>
  );
}
