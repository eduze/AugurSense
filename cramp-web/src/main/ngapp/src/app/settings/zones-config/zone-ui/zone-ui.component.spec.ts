import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ZoneUiComponent } from './zone-ui.component';

describe('ZoneUiComponent', () => {
  let component: ZoneUiComponent;
  let fixture: ComponentFixture<ZoneUiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ZoneUiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ZoneUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
