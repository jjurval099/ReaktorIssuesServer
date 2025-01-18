async function validarFormulario() 
{
  // Obtener los valores de los campos
  const correoDocente = document.getElementById('correoDocente').value ;
  const fechaIncidencia = document.getElementById('fechaIncidencia').value ;
  const numeroAula = document.getElementById('numeroAula').value ;
  const descripcionIncidencia = document.getElementById('descripcionIncidencia').value ;
  const estadoIncidencia = document.getElementById('estadoIncidencia').value ;
  const comentario = document.getElementById('comentario').value ;

  // Validar los campos requeridos
  if (!correoDocente || !fechaIncidencia || !numeroAula || !descripcionIncidencia) 
  {
    alert('Por favor, completa todos los campos obligatorios.');
    return false;
  }

  // Validar formato de correo
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(correoDocente)) 
  {
    alert('Por favor, ingresa un correo válido.');
    return false;
  }

  // Crear un objeto JSON con los datos del formulario
  const data = 
  {
    correoDocente,
    numeroAula,
    fechaIncidencia,
    descripcionIncidencia,
    estadoIncidencia,
    comentario
  };

  try 
  {
    // Enviar los datos a la API
    const response = await fetch('http://localhost:888/incidencias', 
	{
      method: 'POST',
      headers: 
	  {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    if (!response.ok) 
	{
      // Manejo de errores del servidor
      const errorData = await response.json();
      alert('Error al registrar: ' + (errorData.message || 'Error desconocido.'));
      return false;
    }

    // Si el envío fue exitoso
    alert('Registro exitoso. Redirigiendo...');
    window.location.href = "pagina_incidencia.html";
  } 
  catch (error)
  {
    // Manejo de errores de red
    console.error('Error al conectarse a la API:', error);
    alert('Hubo un error al conectarse al servidor. Intenta nuevamente más tarde.');
    return false;
  }
}
