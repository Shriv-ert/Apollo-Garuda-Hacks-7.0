import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import OpenAI from 'openai';
import { EXTRACT_PROMPT, EXTRACT_TEXT_PROMPT } from './llm.constants';

@Injectable()
export class LlmService {
  private openai: OpenAI | null = null;
  private readonly logger = new Logger(LlmService.name);
  private readonly modelName: string;

  constructor(private readonly configService: ConfigService) {
    const apiKey = this.configService.get<string>('LLM_API_KEY');
    const baseURL =
      this.configService.get<string>('LLM_BASE_URL') ||
      'https://ai.sumopod.com/v1';
    this.modelName =
      this.configService.get<string>('LLM_MODEL') ||
      'gemini/gemini-3.1-pro-preview';

    if (apiKey && apiKey.trim() !== '' && apiKey !== 'sk-xxxx') {
      this.openai = new OpenAI({
        apiKey,
        baseURL,
      });
    } else {
      this.logger.warn(
        'LLM_API_KEY is empty or default — POST /check/image will return 503 Service Unavailable',
      );
    }
  }

  get enabled(): boolean {
    return this.openai !== null;
  }

  async extractEntities(
    imageBuffer: Buffer,
    mimeType: string = 'image/jpeg',
  ): Promise<string[]> {
    if (!this.openai) {
      throw new Error('LLM service is not enabled');
    }

    const base64Image = imageBuffer.toString('base64');
    const dataUrl = `data:${mimeType};base64,${base64Image}`;

    const response = await this.openai.chat.completions.create({
      model: this.modelName,
      messages: [
        {
          role: 'user',
          content: [
            { type: 'text', text: EXTRACT_PROMPT },
            {
              type: 'image_url',
              image_url: { url: dataUrl },
            },
          ],
        },
      ],
    });

    const rawText = response.choices[0]?.message?.content?.trim() || '[]';
    return this.parseJsonResponse(rawText);
  }

  async extractEntitiesFromText(text: string): Promise<string[]> {
    if (!this.openai) {
      throw new Error('LLM service is not enabled');
    }

    const response = await this.openai.chat.completions.create({
      model: this.modelName,
      messages: [
        {
          role: 'user',
          content: `${EXTRACT_TEXT_PROMPT}\n\nTeks yang akan diproses:\n${text}`,
        },
      ],
    });

    const rawText = response.choices[0]?.message?.content?.trim() || '[]';
    return this.parseJsonResponse(rawText);
  }

  private parseJsonResponse(rawText: string): string[] {
    const cleanedText = this.stripMarkdownFence(rawText);
    try {
      const parsed = JSON.parse(cleanedText);
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item));
      }
      return [];
    } catch (err) {
      this.logger.error(`Failed to parse LLM json response: ${rawText}`);
      return [];
    }
  }

  private stripMarkdownFence(text: string): string {
    return text
      .replace(/```json/gi, '')
      .replace(/```/g, '')
      .trim();
  }
}
