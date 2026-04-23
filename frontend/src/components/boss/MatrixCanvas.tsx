import { PropsWithChildren, useEffect, useRef } from "react";

type MatrixCanvasProps = PropsWithChildren<{
  fontSize?: number;
  backgroundColor?: string;
  textColor?: string;
  className?: string;
}>;

export default function MatrixCanvas(props: MatrixCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const textColor = props.textColor ?? "#00cc00";
  const backgroundColor = props.backgroundColor ?? "#000000";
  const fontSize = props.fontSize ?? 20;

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let streams: ReturnType<typeof Streams>;
    let animationFrameId: number;
    let frameCount = 0;

    const random = (min: number, max: number) =>
      Math.random() * (max - min) + min;
    const round = (num: number) => Math.round(num);

    const hexToRgb = (hex: string) => {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result
        ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16),
          }
        : { r: 0, g: 255, b: 0 };
    };

    const applyOpacity = (r: number, g: number, b: number, opacity: number) => {
      return `rgba(${r}, ${g}, ${b}, ${opacity})`;
    };

    function setup() {
      if (!canvas || !ctx || !container) return;
      const rect = container.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
      ctx.font = `${fontSize}px monospace`;
      streams = Streams();
    }

    function draw() {
      if (!canvas || !ctx) return;
      frameCount++;
      const bgRgb = hexToRgb(backgroundColor);
      ctx.fillStyle = `rgba(${bgRgb.r}, ${bgRgb.g}, ${bgRgb.b}, 0.6)`;
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      streams.draw();
      animationFrameId = requestAnimationFrame(draw);
    }

    function Streams() {
      if (!canvas) return { draw: () => {} };
      const streams: ReturnType<typeof Stream>[] = [];
      for (let i = 0; i < canvas.width / fontSize; i++) {
        const x = i * fontSize;
        const y = round(random(-1000, 0));
        const speed = 3;
        const length = round(random(10, canvas.height / fontSize));
        const stream = Stream(x, y, speed, length);
        streams.push(stream);
      }

      return {
        draw: function () {
          for (let i = 0, length = streams.length; i < length; i++) {
            streams[i].draw();
          }
        },
      };
    }

    function Stream(x: number, y: number, speed: number, length: number) {
      const symbols: ReturnType<typeof Symbol>[] = [];
      for (let i = 0; i < length; i++) {
        const changeRate = round(random(20, 40));
        let opacity;
        if (i === 0) {
          opacity = 1.0;
        } else if (i === 1) {
          opacity = 0.9;
        } else if (i === 2) {
          opacity = 0.7;
        } else {
          opacity = (1 - i / length) * 0.5;
        }
        const symbol = Symbol(x, y, speed, changeRate, opacity);
        symbols.push(symbol);
        y -= fontSize;
      }

      return {
        draw: function () {
          for (let i = 0, length = symbols.length; i < length; i++) {
            symbols[i].draw();
          }
        },
      };
    }

    function Symbol(
      x: number,
      y: number,
      speed: number,
      changeRate: number,
      opacity: number
    ) {
      let character: string;
      let currentY = y;
      const textRgb = hexToRgb(textColor);

      return {
        draw: function () {
          if (!canvas || !ctx) return;
          if (frameCount % changeRate === 0 || !character) {
            character = String.fromCharCode(round(random(65381, 65440)));
          }
          currentY = currentY > canvas.height + fontSize ? 0 : currentY + speed;
          ctx.fillStyle = applyOpacity(
            textRgb.r,
            textRgb.g,
            textRgb.b,
            opacity
          );
          ctx.fillText(character, x, currentY);
        },
      };
    }

    function handleResize(entries: ResizeObserverEntry[]) {
      if (!canvas || !ctx) return;

      for (const entry of entries) {
        const { width, height } = entry.contentRect;
        canvas.width = width;
        canvas.height = height;
        ctx.font = `${fontSize}px monospace`;
        streams = Streams();
      }
    }

    setup();
    draw();

    const resizeObserver = new ResizeObserver(handleResize);
    resizeObserver.observe(container);

    return () => {
      cancelAnimationFrame(animationFrameId);
      resizeObserver.disconnect();
    };
  }, [fontSize, backgroundColor, textColor]);

  return (
    <div ref={containerRef} className={props.className ?? "w-full h-full"}>
      <canvas ref={canvasRef} className="block w-full h-full" />
    </div>
  );
}
