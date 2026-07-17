import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  pagination?: any;
}

@Injectable()
export class ResponseInterceptor<T>
  implements NestInterceptor<T, ApiResponse<T>>
{
  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<ApiResponse<T>> {
    return next.handle().pipe(
      map((data) => {
        // If data is null or undefined, return standard format
        if (data === null || data === undefined) {
          return {
            success: true,
            message: 'Operation successful',
            data: null,
          };
        }

        // If it's already in the envelope format, return as is
        if (typeof data === 'object' && 'success' in data) {
          return data as any;
        }

        let message = 'Operation successful';
        let result = data;
        let pagination: any = undefined;

        // Extract message, data, and pagination if they are in the returned object
        if (typeof data === 'object') {
          if ('message' in data && 'data' in data) {
            message = data.message;
            result = data.data;
          } else if ('message' in data) {
            message = data.message;
            const { message: _, ...rest } = data;
            result = rest;
          }
          if ('pagination' in data) {
            pagination = data.pagination;
          }
        }

        return {
          success: true,
          message,
          data: result,
          ...(pagination ? { pagination } : {}),
        };
      }),
    );
  }
}
