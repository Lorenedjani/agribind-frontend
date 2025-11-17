import {Injectable} from '@angular/core';
// @ts-ignore
import {HttpEvent, HttpRequest} from '@angular/common/module.d-CnjH8Dlt';
import {HttpHandler, HttpInterceptor} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    if (token) {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
      return next.handle(cloned);
    } else {
      return next.handle(req);
    }
  }
}
