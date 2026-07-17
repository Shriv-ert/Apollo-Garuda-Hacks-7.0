import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import { Response } from 'express';

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  catch(exception: any, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Internal server error';
    let errors: any = null;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const resObj = exception.getResponse();

      if (typeof resObj === 'string') {
        message = resObj;
      } else if (typeof resObj === 'object' && resObj !== null) {
        const tempMsg = (resObj as any).message;
        if (Array.isArray(tempMsg)) {
          message = tempMsg[0] || 'Validation failed';
          errors = tempMsg;
        } else {
          message = tempMsg || exception.message;
          errors = (resObj as any).error || null;
        }
      } else {
        message = exception.message;
      }
    } else {
      message = exception.message || 'Internal server error';
      console.error('Unhandled exception:', exception);
    }

    response.status(status).json({
      success: false,
      message,
      errors,
    });
  }
}
