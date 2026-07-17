document.addEventListener('DOMContentLoaded', () => {
    // Manejo del Slider Antes/Después
    const slider = document.getElementById('compareSlider');
    const filteredImg = document.querySelector('.img-filtered');
    const sliderLine = document.querySelector('.slider-line');

    if (slider) {
        slider.addEventListener('input', (e) => {
            const val = e.target.value;
            filteredImg.style.clipPath = `polygon(0 0, ${val}% 0, ${val}% 100%, 0 100%)`;
            sliderLine.style.left = `${val}%`;
        });
    }

    // Lógica para cambiar de filtro en el Showcase
    const filterSelect = document.getElementById('webFilterSelect');
    const filteredImgEl = document.querySelector('.img-filtered img');
    const filterLabel = document.getElementById('filterLabel');

    if (filterSelect && filteredImgEl) {
        filterSelect.addEventListener('change', (e) => {
            const filterType = e.target.value;
            let cssFilter = '';
            let labelText = '';

            switch(filterType) {
                case 'amanecer':
                    cssFilter = 'sepia(0.8) hue-rotate(-20deg) contrast(1.3) brightness(1.1)';
                    labelText = '1. Amanecer ×10 (Convolución Matricial)';
                    break;
                case 'retro':
                    cssFilter = 'sepia(0.5) contrast(1.2) saturate(1.5) hue-rotate(15deg)';
                    labelText = '2. Efecto Retro 1 (Color Mapping)';
                    break;
                case 'negativo':
                    cssFilter = 'invert(100%) hue-rotate(180deg)';
                    labelText = '3. Filtro Negativo (Inversión XOR)';
                    break;
                case 'blur':
                    cssFilter = 'blur(8px) contrast(1.1) brightness(0.9)';
                    labelText = '4. Vidrio Esmerilado (Desenfoque Espacial)';
                    break;
                case 'hsv':
                    cssFilter = 'saturate(300%) contrast(1.1)';
                    labelText = '5. Sobresaturación HSV (Espacio de Color)';
                    break;
            }

            filteredImgEl.style.filter = cssFilter;
            if (filterLabel) {
                filterLabel.textContent = labelText;
            }
        });
    }

    // Efecto Smooth Scroll para los Enlaces del Navbar
    const links = document.querySelectorAll('.nav-links a[href^="#"]');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80, // Offset del navbar
                    behavior: 'smooth'
                });
            }
        });
    });

    // Pequeño efecto 3D al pasar el mouse por las tarjetas de teoría (opcional extra)
    const cards = document.querySelectorAll('.theory-card');
    cards.forEach(card => {
        card.addEventListener('mousemove', e => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = ((y - centerY) / centerY) * -5;
            const rotateY = ((x - centerX) / centerX) * 5;
            
            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`;
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale3d(1, 1, 1)';
        });
    });

    // --- TERMINAL MOCKUP LOGIC ---
    const btnEjecutar = document.getElementById('btnEjecutar');
    const terminalOverlay = document.getElementById('terminalOverlay');
    const terminalBody = document.getElementById('terminalBody');

    if (btnEjecutar && terminalOverlay) {
        btnEjecutar.addEventListener('click', (e) => {
            e.preventDefault();
            terminalOverlay.classList.add('active');
            terminalBody.innerHTML = ''; // Limpiar consola

            const commands = [
                { text: "> Iniciando secuencia de despliegue ImaGen Studio...", class: "" },
                { text: "> mvn clean install", class: "info" },
                { text: "[INFO] Scanning for projects...", class: "info" },
                { text: "[INFO] Building ImaGen Studio 1.0", class: "info" },
                { text: "[INFO] Compiling 45 source files to /target/classes", class: "info" },
                { text: "[INFO] -------------------------------------------------------", class: "info" },
                { text: "[INFO] BUILD SUCCESS", class: "warn" },
                { text: "[INFO] Total time:  1.245 s", class: "info" },
                { text: "> java -cp target/classes efectos.Main", class: "" },
                { text: "[SYSTEM] JVM Warming up...", class: "info" },
                { text: "[LWJGL] Initializing OpenGL Context (GLFW)...", class: "info" },
                { text: "[UI] Cargando Look & Feel y Acentos de Color...", class: "info" },
                { text: "[CORE] 🚀 LANZANDO MOTOR GRÁFICO EN PANTALLA PRINCIPAL...", class: "warn" }
            ];

            let i = 0;
            function typeLine() {
                if (i < commands.length) {
                    const p = document.createElement('p');
                    p.textContent = commands[i].text;
                    if (commands[i].class) p.className = commands[i].class;
                    terminalBody.appendChild(p);
                    terminalBody.scrollTop = terminalBody.scrollHeight;
                    
                    // Tiempo dinámico para simular compilación real
                    let delay = 300;
                    if (i === 1) delay = 800;
                    if (i === 4) delay = 1200;
                    if (i === 8) delay = 600;

                    i++;
                    setTimeout(typeLine, delay);
                } else {
                    // Al terminar, le damos 1.5s y ocultamos la terminal (asumiendo que aquí el usuario hizo Alt+Tab)
                    setTimeout(() => {
                        terminalOverlay.classList.remove('active');
                    }, 3500);
                }
            }

            // Iniciar la secuencia con un pequeño retraso
            setTimeout(typeLine, 500);
        });
        
        // Cerrar haciendo clic afuera si el usuario se arrepiente
        terminalOverlay.addEventListener('click', (e) => {
            if (e.target === terminalOverlay) {
                terminalOverlay.classList.remove('active');
            }
        });
    }

    // --- COOPER & NORMAN UX LOGIC ---

    // 1. Visibilidad del Sistema (Progreso de lectura) y Botón Volver Arriba
    const progressBar = document.getElementById('scrollProgress');
    const scrollTopBtn = document.getElementById('scrollTopBtn');
    
    window.addEventListener('scroll', () => {
        const totalHeight = document.body.scrollHeight - window.innerHeight;
        const progress = (window.scrollY / totalHeight) * 100;
        if (progressBar) progressBar.style.width = `${progress}%`;

        // Mostrar/Ocultar botón de scroll top
        if (scrollTopBtn) {
            if (window.scrollY > 500) {
                scrollTopBtn.classList.add('visible');
            } else {
                scrollTopBtn.classList.remove('visible');
            }
        }
    });

    if (scrollTopBtn) {
        scrollTopBtn.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // 2. Carga Cognitiva (Reveal On Scroll con IntersectionObserver)
    const revealElements = document.querySelectorAll('.theory-section, .showcase-section, .tech-stack-banner, .shortcuts-section');
    revealElements.forEach(el => el.classList.add('reveal')); // Aplicar clase base dinámicamente

    const revealOptions = {
        threshold: 0.15,
        rootMargin: "0px 0px -50px 0px"
    };

    const revealOnScroll = new IntersectionObserver(function(entries, observer) {
        entries.forEach(entry => {
            if (!entry.isIntersecting) return;
            entry.target.classList.add('active');
            observer.unobserve(entry.target); // Solo animar una vez para no distraer (Cooper)
        });
    }, revealOptions);

    revealElements.forEach(el => revealOnScroll.observe(el));

    // 3. Diccionario Algorítmico y Modal Informativo (Efectos)
    const filterDefinitions = {
        'byn': '<strong>Algoritmo de Umbral:</strong><br>Convierte la imagen a 1-bit evaluando la luminancia de cada píxel contra un umbral (threshold). Si es menor, se apaga (0,0,0); si es mayor, se enciende (255,255,255).',
        'grises': '<strong>Ponderación Lumínica:</strong><br>Transforma canales RGB ponderando sus valores (R*0.3 + G*0.59 + B*0.11) para simular la percepción matemática visual humana del brillo.',
        'retro': '<strong>Color Mapping Matemático:</strong><br>Remapeo de saturación y adición de tonos sepia/cálidos, simulando la degradación química de películas fotográficas vintage.',
        'negativo': '<strong>Inversión de Espectro (XOR Simulado):</strong><br>Operación rápida de inversión calculando matemáticamente: <code>NuevoPíxel = 255 - PíxelOriginal</code> para cada canal RGB.',
        'histograma': '<strong>Análisis Estadístico:</strong><br>Extracción profunda y ploteo del arreglo bidimensional de frecuencias para evaluar el rango dinámico y contraste intrínseco de los canales RGB.',
        'blending': '<strong>Alpha Blending Interpolado:</strong><br>Mezcla matemática de dos mapas de píxeles independientes promediando su peso cromático según un factor Alpha dinámico.',
        
        'convolucion': '<strong>Filtro Espacial Matricial:</strong><br>Multiplicación de vecindarios de píxeles de 3x3 o 5x5 mediante un Kernel convolucional de detección de bordes o realce.',
        'amanecer': '<strong>Sepia Extremo + Exposición:</strong><br>Alteración severa en el espacio de color multiplicando los canales de rojo y verde e inyectando un offset masivo de brillo multiplicativo.',
        'hsv_dinamico': '<strong>Conversión RGB a Cilíndrico:</strong><br>Traducción computacional de coordenadas cartesianas (RGB) a cilíndricas (Hue, Saturation, Value) para aislamiento puro de color y brillo.',
        'saturacion': '<strong>Modulación de Croma:</strong><br>Escalamiento vectorial de la distancia del color respecto al gris central preservando matemáticamente su luminancia original.',
        'brillo': '<strong>Offset de Canal:</strong><br>Suma algorítmica constante y clampada (0-255) a los canales de color para alterar la exposición global.',
        'alpha': '<strong>Inyección Esteganográfica / Transparencia:</strong><br>Operaciones Bitwise (desplazamientos de bits) sobre el canal ARGB de 32 bits (Alpha) manipulando la opacidad absoluta del array.',
        
        'stencil': '<strong>Enmascaramiento Circular:</strong><br>Corte matemático restrictivo usando el cálculo euclidiano del radio: <code>(x-cx)² + (y-cy)² ≤ r²</code> para preservar solo el centro.',
        'vidrio': '<strong>Dispersión Estocástica:</strong><br>Inyección de ruido espacial puro intercambiando píxeles adyacentes de manera aleatoria dentro de un radio límite predefinido.',
        'recorte': '<strong>Máscara de Bits (Bitwise AND):</strong><br>Reducción intencional de la paleta aplicando un enmascaramiento AND lógico para anular los bits menos significativos (efecto de posterización).',
        'aleatorio': '<strong>Motor de Ruido:</strong><br>Llenado de un buffer bidimensional completo con vectores ARGB completamente aleatorizados en ciclos CPU de alta velocidad.',
        'copiar': '<strong>Deep Copy de Memoria:</strong><br>Copia bloque a bloque del array de píxeles original, preservando la matriz primaria como estado de restauración (Ctrl+Z).',
        
        'deg_horizontal': '<strong>Interpolación Lineal en X:</strong><br>Variación paramétrica de color desde <code>x=0</code> hasta <code>x=Width</code> calculando deltas para interpolar el gradiente suavemente.',
        'deg_vertical': '<strong>Interpolación Lineal en Y:</strong><br>Mapeo algorítmico progresivo del color descendiendo píxel a píxel sobre el eje de las ordenadas (Y).',
        'deg_radial': '<strong>Gradiente Euclidiano Centro-Borde:</strong><br>Cálculo de decaimiento lumínico exponencial basado en la distancia radial (pitágoras) entre el píxel actual y el centro absoluto de la matriz.',
        'deg_suavizado': '<strong>Interpolación Radial Bicuadrática:</strong><br>Versión optimizada del gradiente radial que elimina el banding aplicando una función de suavizado (Smoothstep o similar) al decaimiento.'
    };

    const filterItems = document.querySelectorAll('.filter-item');
    const infoModal = document.getElementById('infoModal');
    const infoModalTitle = document.getElementById('infoModalTitle');
    const infoModalDesc = document.getElementById('infoModalDesc');
    const closeInfoModal = document.getElementById('closeInfoModal');

    if (infoModal && filterItems.length > 0) {
        filterItems.forEach(item => {
            item.addEventListener('click', () => {
                const id = item.getAttribute('data-id');
                const title = item.innerText;
                const desc = filterDefinitions[id] || '<em>Definición técnica en construcción...</em>';
                
                infoModalTitle.innerText = title.replace('• ', '');
                infoModalDesc.innerHTML = desc;
                
                infoModal.classList.add('active');
            });
        });

        const hideInfoModal = () => infoModal.classList.remove('active');
        
        if (closeInfoModal) closeInfoModal.addEventListener('click', hideInfoModal);
        
        infoModal.addEventListener('click', (e) => {
            if (e.target === infoModal) hideInfoModal(); // Cerrar al dar clic en el overlay negro
        });
    }

    // 4. Lógica de Retos (Gamificación - Progresión Lineal)
    window.unlockNext = function(nextLevel, btn) {
        const card = btn.closest('.challenge-card');
        const answer = card.querySelector('.challenge-a');
        
        // Mostrar respuesta actual
        if (answer) {
            answer.style.display = 'block';
            answer.style.animation = 'slideUpFade 0.5s ease forwards';
        }
        
        // Ocultar botón actual
        btn.style.display = 'none';

        // Desbloquear siguiente nivel
        if (nextLevel <= 3) {
            const nextCard = document.getElementById('reto' + nextLevel);
            if (nextCard) {
                nextCard.classList.remove('locked');
                const lockIcon = nextCard.querySelector('.lock-icon');
                if (lockIcon) lockIcon.style.display = 'none';
                
                // Hacer scroll suave hacia la siguiente tarjeta (opcional para UX)
                // nextCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        }
    };

    window.checkFinalAnswer = function(btn, isCorrect) {
        if (isCorrect) {
            // Desactivar todos los botones para evitar más clics
            const options = btn.parentElement.querySelectorAll('.option-btn');
            options.forEach(opt => opt.style.pointerEvents = 'none');
            
            // Marcar correcto
            btn.classList.add('correct');
            
            // Mostrar texto de respuesta
            const answer = btn.closest('.challenge-card').querySelector('.challenge-a');
            if (answer) {
                answer.style.display = 'block';
                answer.style.animation = 'slideUpFade 0.5s ease forwards';
            }
            
            // Disparar Efecto WIN
            setTimeout(() => {
                const winOverlay = document.getElementById('winOverlay');
                if (winOverlay) winOverlay.classList.add('active');
            }, 800);
            
        } else {
            // Efecto de error (Shake y Rojo)
            btn.classList.add('wrong');
            setTimeout(() => btn.classList.remove('wrong'), 500);
        }
    };
});
